package de.husterknupp.todoapp

import de.husterknupp.todoapp.configuration.logger
import org.springframework.stereotype.Service
import java.io.File

@Service
class TodoHistory {
    private val log by logger()
    private val todos: Set<Todo>

    init {
        if (!File("./todos-so-far").isFile) {
            log.info("Could not find history file. Created ./todos-so-far")
            File("./todos-so-far").createNewFile()
            todos = mutableSetOf()
        } else {
            log.info("For historical reasons.. using already present file ./todos-so-far")
//            todo JSONify of File("./todos-so-far")
            todos = mutableSetOf()
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
