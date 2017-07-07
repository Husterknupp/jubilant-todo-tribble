package de.husterknupp.todoapp

import de.husterknupp.todoapp.configuration.JiraConfiguration
import de.husterknupp.todoapp.configuration.logger
import khttp.get
import khttp.post
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*

@Service
open class JiraHandler (
        private val jiraConfiguration: JiraConfiguration,
        val todoRepo: TodoRepository
) {
    private val log by logger()
    private val jiraBase = "${jiraConfiguration.url}/rest/api/2"

    @Scheduled(fixedDelay = 1 * 60 * 1000)
    fun entryPoint() {
        todoRepo.getUnnoticedNewTodos().forEach { updateJiraIssue(it) }
    }

    //{ "update": { "comments": [{"edit": {"id": 10002, "body": "newbody"} } ] } }
//    enum class JiraVerb { SET }
//
//    data class JiraDescriptionUpdate(val newDescription: String) {
//        val edit:
//    }

    private fun updateJiraIssue(todo: Todo) {
        /* todo here am I 7.7.17
            http://kelpie9:8081/rest/api/2/issue/BULK-62
            get issue
            parse description
            append given todoo in agreed format
            assemble post request
            fire and forget
         */
        val authHeader = String(Base64.getEncoder().encode("${jiraConfiguration.username}:${jiraConfiguration.password}".toByteArray()))
        val response = get("${jiraBase}issue/${jiraConfiguration.issueId}",
                headers = mapOf("Authorization" to "Basic " + authHeader, "Content-Type" to "application/json"))
        log.info("response: ${response.jsonObject}")
    }

    fun createJiraIssue(issueTitle :String, issueDescription :String) {
        val username = jiraConfiguration.username
        val password =2

        val payload = mapOf("fields" to mapOf("project" to mapOf("id" to jiraConfiguration.projectId), "summary" to issueTitle,
                "description" to issueDescription, "issuetype" to mapOf("id" to jiraConfiguration.issueTypeId),
                "assignee" to mapOf("name" to jiraConfiguration.assignee), "components" to arrayOf( mapOf("id" to jiraConfiguration.componentId))))

        val authHeader = String(Base64.getEncoder().encode("$username:$password".toByteArray()))
        val response = post("$jiraBase/issue/",
                headers = mapOf("Authorization" to "Basic " + authHeader, "Content-Type" to "application/json"),
                json = payload)

        log.info("response: ${response.jsonObject}")
    }

    fun getJiraIssuesOfAssignee() {
        val username = jiraConfiguration.username
        val password =jiraConfiguration.password
        val assignee = jiraConfiguration.assignee

        val authHeader = String(Base64.getEncoder().encode("$username:$password".toByteArray()));
        val response = get("$jiraBase/search?jql=assignee=$assignee",
                headers=mapOf("Authorization" to "Basic " + authHeader, "Content-Type" to "application/json"))
        log.info("response: ${response.jsonObject}")
    }
}
