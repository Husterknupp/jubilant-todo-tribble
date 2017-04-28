package de.husterknupp.todoapp

import de.husterknupp.todoapp.configuration.JiraConfiguration
import org.junit.Ignore
import org.junit.Test

class JiraHandlerTest {

    @Ignore
    @Test
    fun getJiraIssuesOfAssignee() {
        val jiraConfig = JiraConfiguration()
        jiraConfig.url = Credentials().jiraUrl()
        jiraConfig.username = Credentials().jiraUsername()
        jiraConfig.password = Credentials().jiraPassword()
        jiraConfig.assignee = Credentials().jiraAssignee()
        val jiraHandler = JiraHandler(jiraConfig)
        jiraHandler.getJiraIssuesOfAssignee()
    }

    @Test
    @Ignore
    fun createJiraIssue() {
        val jiraConfig = JiraConfiguration()
        jiraConfig.url = Credentials().jiraUrl()
        jiraConfig.username = Credentials().jiraUsername()
        jiraConfig.password = Credentials().jiraPassword()
        jiraConfig.assignee = Credentials().jiraAssignee()
        jiraConfig.issueTypeId = Credentials().jiraIssueTypeId()
        jiraConfig.componentId = Credentials().jiraComponentId()
        jiraConfig.projectId = Credentials().jiraProjectId()
        val jiraHandler = JiraHandler(jiraConfig)
        jiraHandler.createJiraIssue()
    }

}
