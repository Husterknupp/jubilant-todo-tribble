package de.husterknupp.todoapp

import de.husterknupp.todoapp.configuration.GitlabConfiguration
import de.husterknupp.todoapp.configuration.logger
import org.junit.Test

class TodoScannerTest {
    @Test
    fun update() {

        val gitlabConfiguration = GitlabConfiguration()
        gitlabConfiguration.privateToken = Credentials().privateToken()
        gitlabConfiguration.url = Credentials().gitlabUrl()
        val scanner = TodoScanner(gitlabConfiguration)

        scanner.update()

        logger().value.info("HALK")
    }

}
