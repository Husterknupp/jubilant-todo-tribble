package de.husterknupp.todoapp

import de.husterknupp.todoapp.configuration.GitlabConfiguration
import de.husterknupp.todoapp.configuration.logger
import khttp.get
import org.json.JSONObject
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
open class TodoScanner constructor(
        private val gitlabConfiguration: GitlabConfiguration
) {
    private val log by logger()

    init { }

    @Scheduled(fixedDelay = 10000)
    fun update() {
        // todo make repo configurable
        val fileUrls = mutableSetOf<String>()
        val treeUrls = mutableSetOf("${gitlabConfiguration.url}/api/v4/projects/1801/repository/tree")
        while (treeUrls.isNotEmpty()) {
            val res = findTreeAndFileUrls(treeUrls.first())
            log.info("found ${res.fileUrls.size} file(s) and ${res.treeUrls.size} tree(s)")
            treeUrls.remove(treeUrls.first())
            treeUrls.addAll(res.treeUrls)
            fileUrls.addAll(res.fileUrls)
        }
        fileUrls.forEach { url -> log.info(url) }
    }

    private fun findTreeAndFileUrls(initialUrl: String): GitlabDirectory {
        val r = get(initialUrl, params = mapOf("private_token" to gitlabConfiguration.privateToken))
        log.info("requesting ${r.request.url}")
        val files = r.jsonArray
        val fileUrls = mutableSetOf<String>()
        val treeUrls = mutableSetOf<String>()
        for (i in 0..files.length() - 1) {
            val fileOrTree = files.getJSONObject(i)
            val encodedPath: String = encodePunkt(fileOrTree)
            if (fileOrTree.getString("type") == "blob") {
                fileUrls.add(encodedPath)
            } else { // type tree
                treeUrls.add(encodedPath)
//                todo
//                /api/v4/projects/1801/repository/tree?path=src/main&private_token=...
            }
        }
        log.info("found ${fileUrls.size} file(s) and ${treeUrls.size} tree(s)")
        return GitlabDirectory(fileUrls, treeUrls)
    }

    private fun encodePunkt(fileOrTree: JSONObject): String {
        val path = fileOrTree.getString("path")
        val encodedPath: String
        if (path.contains(".")) {
            encodedPath = path.replace(".", "%2E")
        } else {
            encodedPath = path
        }
        return encodedPath
    }

    class GitlabDirectory(val fileUrls: Set<String>, val treeUrls: Set<String>)
}
