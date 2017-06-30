package de.husterknupp.todoapp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.husterknupp.todoapp.configuration.logger
import org.springframework.stereotype.Service
import java.io.File

@Service
class ScannedBranchRepository(repositoryPath: String = "./scanned-branches") {
    private val log by logger()
    private val scannedBranches: MutableSet<ScannedBranch>
    private val scannedBranchesFile: File
    private val mapper: ObjectMapper

    init {
        ObjectMapper().registerModule(KotlinModule())
        mapper = jacksonObjectMapper()
        scannedBranchesFile = File(repositoryPath)
        if (!scannedBranchesFile.isFile) {
            log.info("Could not find file for scanned branches. Created $repositoryPath")
            scannedBranchesFile.createNewFile()
            scannedBranches = mutableSetOf()
            scannedBranchesFile.writeText(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(scannedBranches))
        } else {
            log.info("For historical reasons.. using already present file $repositoryPath")
            val content = scannedBranchesFile.readText()
            scannedBranches = mapper.readValue<MutableSet<ScannedBranch>>(content)
        }
    }

    fun findAllScannedBranches(): Collection<ScannedBranch> {
        return scannedBranches
    }

    fun saveScannedBranch(scannedBranch: ScannedBranch) {
        scannedBranches.add(scannedBranch)
        scannedBranchesFile.writeText(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(scannedBranches))
        log.info("saved new scanned scanned branch $scannedBranch")
    }
}
