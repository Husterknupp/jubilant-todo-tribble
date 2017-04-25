package de.husterknupp.todoapp

import de.husterknupp.todoapp.configuration.JiraConfiguration
import org.junit.Test

class JiraHandlerTest {

    @Test
    fun createJiraIssue() {
        val jiraConfig = JiraConfiguration()
        jiraConfig.url = Credentials().jiraUrl()
        jiraConfig.username = Credentials().jiraUsername()
        jiraConfig.password = Credentials().jiraPassword()
        jiraConfig.assignee = Credentials().jiraAssignee()
        val jiraHandler = JiraHandler(jiraConfig)
        jiraHandler.createJiraIssue()
    }

}