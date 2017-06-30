package de.husterknupp.todoapp

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.husterknupp.todoapp.configuration.GitlabConfiguration
import de.husterknupp.todoapp.configuration.logger
import khttp.get
import org.eclipse.jgit.patch.Patch
import org.json.JSONObject
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

//  todo commit id along with todo so that I can see at what repo version the todo was still there
//  todo scanFileTree Todo<SPACE> instead w/o <SPACE>
//  todo list todos per repoId/branchName
@Service
open class TodoScanner constructor(
        private val gitlabConfiguration: GitlabConfiguration,
        private val todoRepo: TodoRepository
) {
    private val log by logger()
    private val repoBase = gitlabConfiguration.url + "/api/v4/projects/${gitlabConfiguration.repoId}/repository"
    private val mapper: ObjectMapper
    private var scanning = false
    private val scannedBranches = HashSet<ScannedBranch>()

    init {
        ObjectMapper().registerModule(KotlinModule())
        mapper = jacksonObjectMapper()
    }


    @Scheduled(fixedDelay = 5 * 60 * 1000)
    fun entryPoint() {
        if (scanning) {
            log.info("Currently scanning. Skipping this scheduled run")
            return
        }

        scanning = true
        val scannedBranch = findAllScannedBranches()
                .filter {
                    it.repoId == gitlabConfiguration.repoId
                            && it.branchName == gitlabConfiguration.repoBranch
                }.firstOrNull()
        when (scannedBranch) {
            null -> {
                log.info("branch ${gitlabConfiguration.repoBranch} of repo ${gitlabConfiguration.repoId} not scanned yet.")
                log.info("starting initial todo scan of whole file tree now")
                scanFileTree()
                saveScannedBranch(ScannedBranch(gitlabConfiguration.repoId, gitlabConfiguration.repoBranch, Instant.now().toEpochMilli()))
            }
            else -> {
                log.info("Already scanned branch ${gitlabConfiguration.repoBranch} of repo ${gitlabConfiguration.repoId}")
                val dateFormattedUtcScannedLast = Date(scannedBranch.timestampUtcScannedLast).toInstant()
                        .atZone(ZoneId.of("GMT"))
                        .truncatedTo(ChronoUnit.SECONDS).toString()
                log.info("only scanning commits of repo since $dateFormattedUtcScannedLast")
                scanCommits(dateFormattedUtcScannedLast)
                saveScannedBranch(scannedBranch.copy(timestampUtcScannedLast = Instant.now().toEpochMilli()))
            }
        }
        scanning = false
    }

    private fun findAllScannedBranches(): Collection<ScannedBranch> {
//        todo implement file system read
        return scannedBranches
    }

    private fun saveScannedBranch(scannedBranch: ScannedBranch) {
//        todo implement file system save
        scannedBranches.removeIf { it.repoId == scannedBranch.repoId && it.branchName == scannedBranch.branchName }
        scannedBranches.add(scannedBranch)
    }

    /*
        For new repos
        remember current timestamp in format 2012-09-20T11:50:22+03:00
        scanFileTree and save whole file tree for todos as usual
        mark for this repo somewhere 'initialScanDone: true'
        save remembered timestamp along ('lastIntervalScan')

        For all known repos (initialScanDone: true)
        4. remember current timestamp
        5. read remembered timestamp (lastIntervalScan)
        6. read commits since then, scanFileTree and update todos in diffs
        7. save remembered timestamp in lastIntervalScan
     */
    fun scanCommits(dateFormattedUtcScannedLast: String) {
        val timestampTo = Date().toInstant().atZone(ZoneId.of("GMT")).truncatedTo(ChronoUnit.SECONDS).toString()

        val response = get(repoBase + "/commits", params = mapOf(
                "private_token" to gitlabConfiguration.privateToken
                ,"since" to dateFormattedUtcScannedLast
                ,"until" to timestampTo
                ,"ref_name" to gitlabConfiguration.repoBranch))
        log.info("requesting ${response.request.url}")
        mapper.readValue<List<Commit>>(response.text).forEach({ (id) ->
            val diffPayload = get("$repoBase/commits/$id/diff", params = mapOf("private_token" to gitlabConfiguration.privateToken)).text
            mapper.readValue<List<CommitDiff>>(diffPayload)
                    .flatMap { findChangedTodos(it.diff) }
                    .forEach { todoRepo.saveIfNew(it) }
        })
    }

    fun findChangedTodos(diff: String) :List<Todo> {
        val patch = Patch()
        patch.parse(ByteArrayInputStream(diff.toByteArray(StandardCharsets.UTF_8)))
        val result: MutableList<Todo> = ArrayList()
        patch.files.flatMap { fileHeader -> fileHeader.hunks}.forEach { hunk ->
            val lineNoBeforeHunkStarts = String(hunk.buffer).lines().indexOfFirst { line -> line.contains(Regex("@@.*@@")) }
            val hunkLines = String(hunk.buffer).lines().drop(lineNoBeforeHunkStarts + 1)
            var deletedLinesCount = 0
            var addedLinesCount = 0
        //  todo make code easier to read
        //      First have a 'add' representation
        //      Second have a 'delete' representation and find todos respectively
            hunkLines.mapIndexed { index, line ->
                if (line.startsWith("+")) {
                    addedLinesCount++
                    if (stringContainsTodo(line)) {
                        val hunkLinesOnlyAdds = hunkLines.filter { line -> !line.startsWith("-") }.map { line -> line.drop(1) }
                        val todoLineNoInNewFile = hunk.newStartLine + index - deletedLinesCount
                        result.add(Todo(hunk.fileHeader.newPath, todoLineNoInNewFile, line.drop(1), hunkLinesOnlyAdds.joinToString("\n")))
                    }
                } else if (line.startsWith("-")) {
                    deletedLinesCount++
                    if (stringContainsTodo(line)) {
                        val hunkLinesOnlyAdds = hunkLines.filter { line -> !line.startsWith("+") }.map { line -> line.drop(1) }
                        val todoLineNoInNewFile = hunk.oldImage.startLine + index - addedLinesCount
                        result.add(Todo(hunk.fileHeader.oldPath, todoLineNoInNewFile, line.drop(1), hunkLinesOnlyAdds.joinToString("\n"), TodoState.REMOVED_NOT_NOTIFIED))
                    }
                }
            }
        }
        return result
    }

    private fun stringContainsTodo(stringToCheck: String) = stringToCheck.contains("todo ", ignoreCase = true)

    fun scanFileTree() {
        val fileUrls = mutableSetOf<String>()
        val treeUrls = mutableSetOf("")
        while (treeUrls.isNotEmpty()) {
            val res = findTreeAndFileUrls(treeUrls.first())
            log.info("found ${res.fileUrls.size} file(s) and ${res.treeUrls.size} tree(s)")
            treeUrls.remove(treeUrls.first())
            treeUrls.addAll(res.treeUrls)
            fileUrls.addAll(res.fileUrls)
        }
        log.info("found ${fileUrls.size} files")
        fileUrls.forEach { url -> log.info(url) }

        val savedTodos = fileUrls
                .flatMap { url -> downloadAndFindTodos(url) }
                .map { todoRepo.saveIfNew(it) }
        log.info("found ${savedTodos.size} todos (${savedTodos.count { it }} are new)")
    }

    private fun findTreeAndFileUrls(treePath: String): GitlabDirectory {
        val gitlabUrl = "$repoBase/tree"
        val response = get(gitlabUrl, params = mapOf("private_token" to gitlabConfiguration.privateToken, "path" to treePath))
        log.info("requesting ${response.request.url}")
        val files = response.jsonArray
        val fileUrls = mutableSetOf<String>()
        val treeUrls = mutableSetOf<String>()
        for (i in 0..files.length() - 1) {
            val fileOrTree = files.getJSONObject(i)
            val encodedPath: String = encodePunktAndSlash(fileOrTree)
            if (fileOrTree.getString("type") == "blob") {
                fileUrls.add(encodedPath)
            } else { // type tree
                treeUrls.add(fileOrTree.getString("path"))
            }
        }
        return GitlabDirectory(fileUrls, treeUrls)
    }

    private fun downloadAndFindTodos(url: String): List<Todo> {
        val gitlabUrl = "$repoBase/files/$url/raw"
        val r = get(gitlabUrl, params = mapOf("private_token" to gitlabConfiguration.privateToken,
                "ref" to gitlabConfiguration.repoBranch))
        val lines = r.text.lines()
        val todos: MutableList<Todo> = mutableListOf<Todo>()
        for (i in 0..lines.size - 1) {
            val line = lines[i]
            if (stringContainsTodo(line)) {
                log.info("found todo!")
                var context = ""
                for (j in 7 downTo 1) {
                    context += "\n" + lines.getOrElse(i - j, {""})
                }
                context += line
                for (j in 1..7) {
                    context += "\n" + lines.getOrElse(i + j, {""})
                }
                val todo = Todo(url, i + 1, line, context)
                todos.add(todo)
                log.info("found new todo: $todo in ${r.url}")
            }
        }
        return todos
    }

    private fun encodePunktAndSlash(fileOrTree: JSONObject): String {
        val path = fileOrTree.getString("path")
        val encodedPath: String
        encodedPath = path.replace(".", "%2E").replace("/", "%2F")
        return encodedPath
    }
}

data class GitlabDirectory(val fileUrls: Set<String>, val treeUrls: Set<String>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Commit(val id: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CommitDiff(val diff: String)

data class ScannedBranch(val repoId: String, val branchName: String, val timestampUtcScannedLast: Long)
