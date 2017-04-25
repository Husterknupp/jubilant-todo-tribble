package de.husterknupp.todoapp

import de.husterknupp.todoapp.configuration.JiraConfiguration
import org.junit.Test

class JiraHandlerTest {

    @Test
    fun createJiraIssue() {
        val jiraConfig = JiraConfiguration()
        jiraConfig.url = Credentials().jiraUrl()
        val jiraHandler = JiraHandler(jiraConfig)
        jiraHandler.createJiraIssue()
    }

}