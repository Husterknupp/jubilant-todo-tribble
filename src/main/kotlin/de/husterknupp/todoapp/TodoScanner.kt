package de.husterknupp.todoapp

import de.husterknupp.todoapp.configuration.GitlabConfiguration
import de.husterknupp.todoapp.configuration.logger
import khttp.get
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
        val r = get("${gitlabConfiguration.url}/api/v4/projects/1801/repository/tree", params = mapOf("private_token" to gitlabConfiguration.privateToken))
        val jsonObjectRead = r.jsonArray
        for (i in 0..jsonObjectRead.length() ){
            val arraypart = jsonObjectRead.getJSONObject(i)
            if(arraypart.getString("type") == "blob") {
                val path = arraypart.getString("path")
                val encodedPath:String
                if (path.contains(".")) {
                    encodedPath = path.replace(".", "%2E")
                } else {
                    encodedPath = path
                }
            // todo make repo configurable
                val q = get("${gitlabConfiguration.url}/api/v4/projects/1801/repository/files/$encodedPath/raw",
                        params = mapOf("private_token" to gitlabConfiguration.privateToken, "ref" to "master"))
                log.info(q.text.replaceAfter("\n",""))
                break
            }
        }
    }
}
