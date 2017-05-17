package de.husterknupp.todoapp

import de.husterknupp.todoapp.configuration.GitlabConfiguration
import de.husterknupp.todoapp.configuration.logger
import org.eclipse.jgit.patch.Patch
import org.junit.Ignore
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals

class TodoScannerTest {
    private val log by logger()

    @Ignore
    @Test
    fun scan() {
        val scanner = TodoScanner(loadApplicationYaml(), TodoHistory())
        scanner.scan()
    }

// todo test for edit line
// todo test for removed line

    @Test
    fun findNewTodoInLine3() {
        val diffLines = File("src/test/resources/diff-add-todo-line").readText()
        val patch = Patch()
        val scanner = TodoScanner(loadApplicationYaml(), TodoHistory())

        patch.parse(ByteArrayInputStream(diffLines.toByteArray(StandardCharsets.UTF_8)))

        val newTodo = scanner.findNewTodos(diffLines)
        assertEquals(newTodo.size, 1)
        assertEquals(newTodo[0].lineOfCode, 3)
    }

    @Test
    fun patchTest_jgit() {
        val diffLines = File("src/test/resources/diff-add-todo-line").readText()
        val patch = Patch()
        val parseUnifiedDiff = patch.parse(ByteArrayInputStream(diffLines.toByteArray(StandardCharsets.UTF_8)))
        println()
    }

    fun loadApplicationYaml(): GitlabConfiguration {
        val yamlConfig = Yaml("application.yaml", "gitlab")
        val configuration = GitlabConfiguration()
        configuration.privateToken = yamlConfig.get("privateToken")
        configuration.url = yamlConfig.get("url")
        configuration.repoId = yamlConfig.get("repoId")
        return configuration
    }
}
