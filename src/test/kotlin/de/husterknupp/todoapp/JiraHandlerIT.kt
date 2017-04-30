package de.husterknupp.todoapp

import de.husterknupp.todoapp.configuration.JiraConfiguration
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class JiraHandlerIT {

    @Ignore
    @Test
    fun getJiraIssuesOfAssignee() {
        val jiraHandler = JiraHandler(JiraConfiguration())
        jiraHandler.getJiraIssuesOfAssignee()
    }

    @Test
    fun createJiraIssue() {
        val jiraHandler = JiraHandler(JiraConfiguration())
        jiraHandler.createJiraIssue()
    }

}
