package de.husterknupp.todoapp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.husterknupp.todoapp.configuration.logger
import org.springframework.stereotype.Service
import java.io.File

@Service
class TodoRepository(historyPath: String = "./todos-so-far") {
    private val log by logger()
    private val todos: MutableMap<Int, Todo> // hash -> todoo object
    private val historyFile: File
    private val mapper: ObjectMapper

    init {
        ObjectMapper().registerModule(KotlinModule())
        mapper = jacksonObjectMapper()
        historyFile = File(historyPath)
        if (!historyFile.isFile) {
            log.info("Could not find history file. Created $historyPath")
            historyFile.createNewFile()
            todos = mutableMapOf()
            historyFile.writeText(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(todos))
        } else {
            log.info("For historical reasons.. using already present file $historyPath")
            val content = historyFile.readText()
            todos = mapper.readValue<MutableMap<Int, Todo>>(content)
        }
    }

    fun saveIfNew(todo: Todo): Boolean {
        if(!todos.contains(todo.hashCode())) {
            save(todo)
            log.info("saved new todo $todo")
            return true
        } else {
            return false
        }
    }

    private fun save(todo: Todo) {
        todos.put(todo.hashCode(), todo)
        historyFile.writeText(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(todos))
    }

    fun getUnnoticedNewTodos(): Set<Todo> {
        return todos.values.filter { todo -> todo.isNew() }.toHashSet()
    }

    fun markAsNoticed(todo: Todo, jiraIssueId: String) {
        save(todo.copy(state = TodoState.NEW_NOTIFIED, jiraIssueId = jiraIssueId))
    }
}
