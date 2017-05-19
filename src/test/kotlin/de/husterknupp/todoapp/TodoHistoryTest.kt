package de.husterknupp.todoapp

import de.husterknupp.todoapp.TodoState.NEW_NOTIFIED
import de.husterknupp.todoapp.TodoState.NEW_NOT_NOTIFIED
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class TodoHistoryTest {
    val testHistoryFile = File("./test-history")

    @Before
    fun removeBefore() {
        if (testHistoryFile.exists()) {
            testHistoryFile.delete()
        }
    }

    @After
    fun removeAfterLast() {
        if (testHistoryFile.exists()) {
            testHistoryFile.delete()
        }
    }

    @Test
    fun testSaveIfNew() {
        val todo = Todo("url", 1, "todo refactor this", "big huge context", NEW_NOT_NOTIFIED, "")
        TodoHistory("./test-history").saveIfNew(todo)

        assertTrue("should find hash code in history file",
                { testHistoryFile.readText().contains(todo.hashCode().toString()) })
        assertTrue { testHistoryFile.readText().contains("url") }
        assertTrue { testHistoryFile.readText().contains("1") }
        assertTrue { testHistoryFile.readText().contains("todo refactor this") }
        assertTrue { testHistoryFile.readText().contains("big huge context") }
    }

    @Test
    fun dontOverwriteIfPresent() {
        testHistoryFile
                .writeText("{\"7164087\":{\"fileUrl\":\"url\"" +
                        ",\"lineOfCode\":1,\"todoLineStr\":\"todo\"," +
                        "\"context\":\"big huge context\"" +
                        ",\"state\":NEW_NOT_NOTIFIED,\"jiraIssueId\":\"\"}}")

        val todo = Todo("url", 1, "todo", "changed context", NEW_NOT_NOTIFIED, "")
        TodoHistory("./test-history").saveIfNew(todo)

        assertTrue("should find hash code in history file",
                { testHistoryFile.readText().contains(todo.hashCode().toString()) })
        assertTrue { testHistoryFile.readText().contains("url") }
        assertTrue { testHistoryFile.readText().contains("1") }
        assertTrue { testHistoryFile.readText().contains("todo") }
        assertTrue { testHistoryFile.readText().contains("big huge context") }
        assertFalse { testHistoryFile.readText().contains("changed context") }
    }

    @Test
    fun testGetUnnoticedTodos() {
        val todoHistory = TodoHistory("./test-history")
        val unoticedTodo = Todo("url", 1, "todo", "changed context", state = NEW_NOT_NOTIFIED, jiraIssueId = "")
        todoHistory.saveIfNew(unoticedTodo)
        val noticedTodo = Todo("other file", 1, "other todo", "changed context", state = NEW_NOTIFIED, jiraIssueId = "")
        todoHistory.saveIfNew(noticedTodo)

        assertEquals(todoHistory.getUnnoticedNewTodos().size, 1)
    }

    @Test
    fun testMarkAsNoticed() {
        val todoHistory = TodoHistory("./test-history")
        val todo = Todo("url", 1, "todo", "changed context", state = NEW_NOT_NOTIFIED, jiraIssueId = "")
        todoHistory.saveIfNew(todo)

        assertEquals(todoHistory.getUnnoticedNewTodos().size, 1)
        todoHistory.markAsNoticed(todo, "DEV-123")

        assertEquals(todoHistory.getUnnoticedNewTodos().size, 0)
        assertTrue { testHistoryFile.readText().contains("DEV-123") }
    }

    @Test
    fun testMarkAsNoticedSavesAlsoNewTodos() {
        val todoHistory = TodoHistory("./test-history")
        val todo = Todo("url", 1, "todo", "changed context", state = NEW_NOT_NOTIFIED, jiraIssueId = "")

        assertFalse { testHistoryFile.readText().contains("DEV-123") }
        todoHistory.markAsNoticed(todo, "DEV-123")

        assertTrue { testHistoryFile.readText().contains("DEV-123") }
    }
}
