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

    init {
    }

    @Scheduled(fixedDelay = 10000)
    fun update() {
        // todo make repo configurable
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
    }

    private fun findTreeAndFileUrls(treePath: String): GitlabDirectory {
        val gitlabUrl = "${gitlabConfiguration.url}/api/v4/projects/1801/repository/tree"
        val r = get(gitlabUrl, params = mapOf("private_token" to gitlabConfiguration.privateToken, "path" to treePath))
        log.info("requesting ${r.request.url}")
        val files = r.jsonArray
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

    private fun encodePunktAndSlash(fileOrTree: JSONObject): String {
        val path = fileOrTree.getString("path")
        val encodedPath: String
        encodedPath = path.replace(".", "%2E").replace("/", "%2F")
        return encodedPath
    }

    class GitlabDirectory(val fileUrls: Set<String>, val treeUrls: Set<String>)
}
