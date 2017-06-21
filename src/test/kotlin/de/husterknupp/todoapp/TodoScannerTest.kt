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
import kotlin.test.assertTrue

class TodoScannerTest {
    private val log by logger()

    @Ignore
    @Test
    fun scan() {
        val scanner = TodoScanner(loadApplicationYaml(), TodoHistory())
        scanner.scanFileTree()
    }

    @Test
    fun yeyIknowHowToUseRegex() {
        assertTrue("@@ -1,3 +1,4 @@".contains(Regex("@@.*@@")))
    }

//    todo two todos
//    todo two changes, todo change is the second
//    todo one change, no todo

    @Test
    fun findNewTodoInLine3() {
        val diffLines = File("src/test/resources/diff-add-todo-line").readText()
        val patch = Patch()
        val scanner = TodoScanner(loadApplicationYaml(), TodoHistory())

        patch.parse(ByteArrayInputStream(diffLines.toByteArray(StandardCharsets.UTF_8)))

        val todos = scanner.findChangedTodos(diffLines)
        assertEquals(todos.size, 1)
        assertEquals(todos[0].lineOfCode, 3)
        assertEquals(todos[0].state, TodoState.NEW_NOT_NOTIFIED)
        assertEquals(todos[0].context, "bogus bla bla bla\nbogus bla bla bla\ntodo fix some time\nbogus bla bla bla\n")
    }

    @Test
    fun findRemovedTodoInLine3() {
        val diffLines = File("src/test/resources/diff-remove-todo-line").readText()
        val patch = Patch()
        val scanner = TodoScanner(loadApplicationYaml(), TodoHistory())

        patch.parse(ByteArrayInputStream(diffLines.toByteArray(StandardCharsets.UTF_8)))

        val todos = scanner.findChangedTodos(diffLines)
        assertEquals(todos.size, 1)
        assertEquals(todos[0].lineOfCode, 3)
        assertEquals(todos[0].state, TodoState.REMOVED_NOT_NOTIFIED)
    }

    @Test
    fun findEditedTodoInLine3() {
        val diffLines = File("src/test/resources/diff-edit-todo-line").readText()
        val patch = Patch()
        val scanner = TodoScanner(loadApplicationYaml(), TodoHistory())

        patch.parse(ByteArrayInputStream(diffLines.toByteArray(StandardCharsets.UTF_8)))

        val todos = scanner.findChangedTodos(diffLines)
        assertEquals(todos.size, 2)
        assertEquals(todos[0].lineOfCode, 3)
        assertEquals(todos[0].state, TodoState.REMOVED_NOT_NOTIFIED)
        assertEquals(todos[1].lineOfCode, 3)
        assertEquals(todos[1].state, TodoState.NEW_NOT_NOTIFIED)
    }

    @Test
    fun findNewTodoInNewFileInLine3() {
        val diffLines = File("src/test/resources/diff-add-todo-file").readText()
        val patch = Patch()
        val scanner = TodoScanner(loadApplicationYaml(), TodoHistory())

        patch.parse(ByteArrayInputStream(diffLines.toByteArray(StandardCharsets.UTF_8)))

        val todos = scanner.findChangedTodos(diffLines)
        assertEquals(todos.size, 1)
        assertEquals(todos[0].lineOfCode, 3)
        assertEquals(todos[0].state, TodoState.NEW_NOT_NOTIFIED)
    }

    @Test
    fun findRemovedTodoInRemovedFileInLine3() {
        val diffLines = File("src/test/resources/diff-remove-todo-file").readText()
        val patch = Patch()
        val scanner = TodoScanner(loadApplicationYaml(), TodoHistory())

        patch.parse(ByteArrayInputStream(diffLines.toByteArray(StandardCharsets.UTF_8)))

        val todos = scanner.findChangedTodos(diffLines)
        assertEquals(todos.size, 1)
        assertEquals(todos[0].lineOfCode, 3)
        assertEquals(todos[0].state, TodoState.REMOVED_NOT_NOTIFIED)
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
