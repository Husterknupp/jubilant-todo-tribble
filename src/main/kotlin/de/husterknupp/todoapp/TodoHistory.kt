package de.husterknupp.todoapp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.husterknupp.todoapp.configuration.logger
import org.springframework.stereotype.Service
import java.io.File

@Service
class TodoHistory(historyPath: String) {
    private val log by logger()
    private val todos: MutableMap<Int, Todo> // hash -> todoo object
    private val historyFile: File
    val mapper: ObjectMapper

    init {
        ObjectMapper().registerModule(KotlinModule())
        mapper = jacksonObjectMapper()
        val file = File(historyPath)
        historyFile = file
        if (!file.isFile) {
            log.info("Could not find history file. Created $historyPath")
            file.createNewFile()
            todos = mutableMapOf()
            historyFile.writeText(mapper.writeValueAsString(todos))
        } else {
            log.info("For historical reasons.. using already present file $historyPath")
            val content = file.readText()
            todos = mapper.readValue<MutableMap<Int, Todo>>(content)
        }
    }

    fun saveIfNew(todo: Todo) {
        if(!todos.contains(todo.hashCode())) {
            println("new entry")
            todos.put(todo.hashCode(), todo)
            historyFile.writeText(mapper.writeValueAsString(todos))
        }
    }

    fun getUnnoticedTodos(todo: Todo): Set<Todo> {
        return mutableSetOf()
    }

    fun markAsNoticed(todo: Todo, jiraTicketId: String) {
    }
}
