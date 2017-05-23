package de.husterknupp.todoapp

import de.husterknupp.todoapp.configuration.GitlabConfiguration
import de.husterknupp.todoapp.configuration.logger
import khttp.get
import org.eclipse.jgit.patch.Patch
import org.json.JSONObject
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

//  todo after a commit has been scanned the commit hash/timestamp should be saved
//  todo commit id along with todo so that I can see at what repo version the todo was still there
//  todo fix missing context - don't juggle with add/del at the same time.
//      First have a 'add' representation
//      Second have a 'delete' representation and find todos respectively

@Service
open class TodoScanner constructor(
        private val gitlabConfiguration: GitlabConfiguration,
        private val todoHistory: TodoHistory
) {
    private val log by logger()
    private val repoSegment: String = "/api/v4/projects/${gitlabConfiguration.repoId}/repository"

    fun findChangedTodos(diff: String) :List<Todo> {
        val patch = Patch()
        patch.parse(ByteArrayInputStream(diff.toByteArray(StandardCharsets.UTF_8)))
        val result: MutableList<Todo> = ArrayList()
        patch.files.flatMap { fileHeader -> fileHeader.hunks}.forEach { hunk ->
            val lineNoBeforeHunkStarts = String(hunk.buffer).lines().indexOfFirst { line -> line.contains(Regex("@@.*@@")) }
            val hunkLines = String(hunk.buffer).lines().drop(lineNoBeforeHunkStarts + 1)
            var deletedLinesCount = 0
            var addedLinesCount = 0
            hunkLines.mapIndexed { index, line ->
                if (line.startsWith("+")) {
                    addedLinesCount++
                    if (line.contains("todo", ignoreCase = true)) {
                        val hunkLinesOnlyAdds = hunkLines.filter { line -> !line.startsWith("-") }.map { line -> line.drop(1) }
                        val todoLineNoInNewFile = hunk.newStartLine + index - deletedLinesCount
                        result.add(Todo(hunk.fileHeader.newPath, todoLineNoInNewFile, line.drop(1), hunkLinesOnlyAdds.joinToString("\n")))
                    }
                } else if (line.startsWith("-")) {
                    deletedLinesCount++
                    if (line.contains("todo", ignoreCase = true)) {
                        val hunkLinesOnlyAdds = hunkLines.filter { line -> !line.startsWith("+") }.map { line -> line.drop(1) }
                        val todoLineNoInNewFile = hunk.oldImage.startLine + index - addedLinesCount
                        result.add(Todo(hunk.fileHeader.oldPath, todoLineNoInNewFile, line.drop(1), hunkLinesOnlyAdds.joinToString("\n"), TodoState.REMOVED_NOT_NOTIFIED))
                    }
                }
            }
        }
        return result
    }

    @Scheduled(fixedDelay = 10000)
    fun scan() {
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
                .map { todoHistory.saveIfNew(it) }
        log.info("found ${savedTodos.size} todos (${savedTodos.count { it }} are new)")
    }

    private fun findTreeAndFileUrls(treePath: String): GitlabDirectory {
        val gitlabUrl = "${gitlabConfiguration.url}$repoSegment/tree"
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
        val gitlabUrl = "${gitlabConfiguration.url}$repoSegment/files/$url/raw"
        val r = get(gitlabUrl, params = mapOf("private_token" to gitlabConfiguration.privateToken, "ref" to "master"))
        val lines = r.text.lines()
        val todos: MutableList<Todo> = mutableListOf<Todo>()
        for (i in 0..lines.size - 1) {
            val line = lines[i]
            if (line.contains("todo", ignoreCase = true)) {
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

    data class GitlabDirectory(val fileUrls: Set<String>, val treeUrls: Set<String>)
}
