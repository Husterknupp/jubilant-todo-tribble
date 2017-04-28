package de.husterknupp.todoapp

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TodoTest {
    @Test
    fun testHashCode() {
        assertEquals(Todo("url", 1, "todo", "context", false, "").hashCode(), Todo("url", 1, "todo", "context", false, "").hashCode())
        assertEquals(Todo("url", 2, "todo", "context", false, "").hashCode(), Todo("url", 1, "todo", "context", false, "").hashCode())
        assertEquals(Todo("url", 1, "todo", "changedContext", false, "").hashCode(), Todo("url", 1, "todo", "context", false, "").hashCode())
        assertNotEquals(Todo("otherFile", 1, "todo", "context", false, "").hashCode(), Todo("url", 1, "todo", "context", false, "").hashCode())
        assertNotEquals(Todo("url", 1, "other todo", "context", false, "").hashCode(), Todo("url", 1, "todo", "context", false, "").hashCode())
    }

    @Test
    fun testEquals() {
        assertEquals(Todo("url", 1, "todo", "context", false, ""), Todo("url", 1, "todo", "context", false, ""))
        assertEquals(Todo("url", 2, "todo", "context", false, ""), Todo("url", 1, "todo", "context", false, ""))
        assertEquals(Todo("url", 1, "todo", "changedContext", false, ""), Todo("url", 1, "todo", "context", false, ""))
        assertNotEquals(Todo("otherFile", 1, "todo", "context", false, ""), Todo("url", 1, "todo", "context", false, ""))
        assertNotEquals(Todo("url", 1, "other todo", "context", false, ""), Todo("url", 1, "todo", "context", false, ""))
    }
}
