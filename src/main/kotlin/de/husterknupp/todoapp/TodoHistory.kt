package de.husterknupp.todoapp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.husterknupp.todoapp.configuration.logger
import org.springframework.stereotype.Service
import java.io.File

@Service
class TodoHistory {
    private val log by logger()
    private val todos: Map<Int, Todo> // hash -> todoo object

    init {
        ObjectMapper().registerModule(KotlinModule())
        val mapper = jacksonObjectMapper()
        if (!File("./todos-so-far").isFile) {
            log.info("Could not find history file. Created ./todos-so-far")
            File("./todos-so-far").createNewFile()
            todos = mutableMapOf()
        } else {
            log.info("For historical reasons.. using already present file ./todos-so-far")
//            todo JSONify of File("./todos-so-far")
//            todos = mutableSetOf()
            val content = File("./todos-so-far").readLines().reduce { acc, s -> acc + s}
            todos = mapper.readValue<Map<Int, Todo>>(content)
            log.info(todos.getValue(123456).toString())
        }
    }

    fun saveIfNew(todo: Todo) {
//        log.info(File("./application.yaml").readLines()[1])

        // TodoHistory.todoIsNew {
        //   read history file
        //   read hashes from file
        //   build hash from given todoo
        //   compare against given hashes from file
        //   if todoo hash is new
        //     serialize todoo to json
        //     store serialized todoo along w/ hash
        // }
    }

    fun getUnnoticedTodos(todo: Todo): Set<Todo> {
        return mutableSetOf()
    }

    fun markAsNoticed(todo: Todo, jiraTicketId: String) {
    }
}
