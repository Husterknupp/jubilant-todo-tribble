package de.husterknupp.todoapp.gateway

import de.husterknupp.todoapp.configuration.GitlabConfiguration
import de.husterknupp.todoapp.configuration.logger
import de.husterknupp.todoapp.model.BuildInfo
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
open class GitlabGateway constructor(
        private val gitlabConfiguration: GitlabConfiguration,
        private val restTemplate: RestTemplate
) {

    private val log by logger()

    fun getStatus(): HttpStatus {
        try {
            return restTemplate.getForEntity(gitlabConfiguration.url + "/status", String::class.java).statusCode
        } catch (e: Exception) {
            log.error("Error retrieving status of Gitlab", e)
            return HttpStatus.INTERNAL_SERVER_ERROR
        }
    }

    fun getInfo(): BuildInfo {
        try {
            return restTemplate.getForEntity(gitlabConfiguration.url + "/version", BuildInfo::class.java).body
        } catch (e: Exception) {
            log.error("Error retrieving build info of Gitlab", e)
            return BuildInfo()
        }
    }
}
