package de.husterknupp.todoapp

import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TodoHistoryTest {

    @Before
    fun removeHistoryFile() {
        File("./test-history").deleteOnExit()
    }

    @Test
    fun testSaveIfNew() {
        val todo = Todo("url", 1, "todo refactor this", "big huge context")
        TodoHistory("./test-history").saveIfNew(todo)

        assertTrue("should find hash code in history file",
                { File("./test-history").readText().contains(todo.hashCode().toString()) })
        assertTrue { File("./test-history").readText().contains("url") }
        assertTrue { File("./test-history").readText().contains("1") }
        assertTrue { File("./test-history").readText().contains("todo refactor this") }
        assertTrue { File("./test-history").readText().contains("big huge context") }
    }

    @Test
    fun dontOverwriteIfPresent() {
        File("./test-history")
                .writeText("{\"7164087\":{\"fileUrl\":\"url\",\"lineOfCode\":1,\"todoLineStr\":\"todo\",\"context\":\"big huge context\"}}")

        val todo = Todo("url", 1, "todo", "changed context")
        TodoHistory("./test-history").saveIfNew(todo)

        assertTrue("should find hash code in history file",
                { File("./test-history").readText().contains(todo.hashCode().toString()) })
        assertTrue { File("./test-history").readText().contains("url") }
        assertTrue { File("./test-history").readText().contains("1") }
        assertTrue { File("./test-history").readText().contains("todo") }
        assertTrue { File("./test-history").readText().contains("big huge context") }
        assertFalse { File("./test-history").readText().contains("changed context") }

    }

}
