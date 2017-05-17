package de.husterknupp.todoapp

import de.husterknupp.todoapp.configuration.GitlabConfiguration
import de.husterknupp.todoapp.configuration.logger
import khttp.get
import org.json.JSONObject
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
open class TodoScanner constructor(
        private val gitlabConfiguration: GitlabConfiguration,
        private val todoHistory: TodoHistory
) {
    private val log by logger()
    private val repoSegment: String = "/api/v4/projects/${gitlabConfiguration.repoId}/repository"

    fun findNewTodos(diff: String) :List<Todo> {
        return emptyList()
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
                val todo = Todo(url, i + 1, line, context, false, "")
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
