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


class TodoRepositoryTest {
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
        TodoRepository("./test-history").saveIfNew(todo)

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
                        ",\"state\":\"NEW_NOT_NOTIFIED\",\"jiraIssueId\":\"\"}}")
        val todo = Todo("url", 1, "todo", "changed context", NEW_NOT_NOTIFIED, "")
        TodoRepository("./test-history").saveIfNew(todo)

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
        val todorepo = TodoRepository("./test-history")
        val unoticedTodo = Todo("url", 1, "todo", "changed context", state = NEW_NOT_NOTIFIED, jiraIssueId = "")
        todorepo.saveIfNew(unoticedTodo)
        val noticedTodo = Todo("other file", 1, "other todo", "changed context", state = NEW_NOTIFIED, jiraIssueId = "")
        todorepo.saveIfNew(noticedTodo)

        assertEquals(todorepo.getUnnoticedNewTodos().size, 1)
    }

    @Test
    fun testMarkAsNoticed() {
        val todoRepo = TodoRepository("./test-history")
        val todo = Todo("url", 1, "todo", "changed context", state = NEW_NOT_NOTIFIED, jiraIssueId = "")
        todoRepo.saveIfNew(todo)

        assertEquals(todoRepo.getUnnoticedNewTodos().size, 1)
        todoRepo.markAsNoticed(todo, "DEV-123")

        assertEquals(todoRepo.getUnnoticedNewTodos().size, 0)
        assertTrue { testHistoryFile.readText().contains("DEV-123") }
    }

    @Test
    fun testMarkAsNoticedSavesAlsoNewTodos() {
        val todoRepo = TodoRepository("./test-history")
        val todo = Todo("url", 1, "todo", "changed context", state = NEW_NOT_NOTIFIED, jiraIssueId = "")

        assertFalse { testHistoryFile.readText().contains("DEV-123") }
        todoRepo.markAsNoticed(todo, "DEV-123")

        assertTrue { testHistoryFile.readText().contains("DEV-123") }
    }
}
