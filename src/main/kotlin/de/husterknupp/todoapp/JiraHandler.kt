package de.husterknupp.todoapp

import de.husterknupp.todoapp.configuration.JiraConfiguration
import de.husterknupp.todoapp.configuration.logger
import khttp.get
import org.springframework.stereotype.Service
import java.util.*

@Service
open class JiraHandler constructor(
        private val jiraConfiguration: JiraConfiguration
) {
    private val log by logger()
    private val repoSegment: String = "/rest/api/2/"

    init {
    }

    fun createJiraIssue() {
        val username = jiraConfiguration.username
        val password =jiraConfiguration.password
        val assignee = jiraConfiguration.assignee

        val authHeader = String(Base64.getEncoder().encode("$username:$password".toByteArray()));
        val response = get("${jiraConfiguration.url}${repoSegment}search?jql=assignee=$assignee", headers=mapOf("Authorization" to "Basic " + authHeader, "Content-Type" to "application/json"))
        log.info("response: ${response.jsonObject}")
    }
}