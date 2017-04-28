package de.husterknupp.todoapp

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TodoTest {
    @Test
    fun testHashCode() {
        assertEquals(Todo("url", 1, "todo", "context").hashCode(), Todo("url", 1, "todo", "context").hashCode())
        assertEquals(Todo("url", 2, "todo", "context").hashCode(), Todo("url", 1, "todo", "context").hashCode())
        assertEquals(Todo("url", 1, "todo", "changedContext").hashCode(), Todo("url", 1, "todo", "context").hashCode())
        assertNotEquals(Todo("otherFile", 1, "todo", "context").hashCode(), Todo("url", 1, "todo", "context").hashCode())
        assertNotEquals(Todo("url", 1, "other todo", "context").hashCode(), Todo("url", 1, "todo", "context").hashCode())
    }

    @Test
    fun testEquals() {
        assertEquals(Todo("url", 1, "todo", "context"), Todo("url", 1, "todo", "context"))
        assertEquals(Todo("url", 2, "todo", "context"), Todo("url", 1, "todo", "context"))
        assertEquals(Todo("url", 1, "todo", "changedContext"), Todo("url", 1, "todo", "context"))
        assertNotEquals(Todo("otherFile", 1, "todo", "context"), Todo("url", 1, "todo", "context"))
        assertNotEquals(Todo("url", 1, "other todo", "context"), Todo("url", 1, "todo", "context"))
    }
}
