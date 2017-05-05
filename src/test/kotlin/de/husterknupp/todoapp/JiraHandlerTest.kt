package de.husterknupp.todoapp

import de.husterknupp.todoapp.configuration.JiraConfiguration
import org.junit.Ignore
import org.junit.Test

class JiraHandlerTest {

    @Ignore
    @Test
    fun getJiraIssuesOfAssignee() {
        val jiraHandler = JiraHandler(loadJiraYaml())
        jiraHandler.getJiraIssuesOfAssignee()
    }

    @Ignore
    @Test
    fun createJiraIssue() {
        val jiraHandler = JiraHandler(loadJiraYaml())
        jiraHandler.createJiraIssue("", "")
    }

    fun loadJiraYaml(): JiraConfiguration {
        val yamlConfig = Yaml("application.yaml", "jira")
        val configuration = JiraConfiguration()
        configuration.assignee = yamlConfig.get("assignee")
        configuration.componentId = yamlConfig.get("componentId")
        configuration.issueTypeId = yamlConfig.get("issueTypeId")
        configuration.projectId = yamlConfig.get("projectId")
        configuration.password = yamlConfig.get("password")
        configuration.username = yamlConfig.get("username")
        configuration.url = yamlConfig.get("url")
        return configuration
    }

}
