package de.husterknupp.todoapp

import de.husterknupp.todoapp.configuration.GitlabConfiguration
import de.husterknupp.todoapp.configuration.logger
import org.junit.Ignore
import org.junit.Test

class TodoScannerTest {
    private val log by logger()

    @Ignore
    @Test
    fun scan() {
        val scanner = TodoScanner(loadApplicationYaml())
        scanner.scan()
    }

    fun loadApplicationYaml(): GitlabConfiguration {
        val yamlConfig = Yaml("application.yaml", "gitlab")
        val configuration = GitlabConfiguration()
        configuration.privateToken = yamlConfig.get("privateToken")
        configuration.url = yamlConfig.get("url")
        return configuration
    }
}
