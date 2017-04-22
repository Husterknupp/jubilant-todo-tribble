package de.husterknupp.todoapp.controller

import de.husterknupp.todoapp.configuration.InfoConfiguration
import de.husterknupp.todoapp.configuration.security.DisableAutoAuth
import de.husterknupp.todoapp.health.GitlabHealth
import de.husterknupp.todoapp.health.JubilantTodoTribbleHealth
import de.husterknupp.todoapp.health.ServiceHealth
import de.husterknupp.todoapp.model.BuildInfo
import de.husterknupp.todoapp.model.ServiceStatus
import de.husterknupp.todoapp.model.StatusOverviewResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
@DisableAutoAuth
class InfoController constructor(
        private val jubilantTodoTribbleHealth: JubilantTodoTribbleHealth,
        private val gitlabHealth: GitlabHealth,
        private val infoConfiguration: InfoConfiguration
) {

    @GetMapping("version")
    fun getVersion(): ResponseEntity<BuildInfo> {
        return ResponseEntity(infoConfiguration.build, HttpStatus.OK)
    }

    @GetMapping("status")
    fun getStatus() : ResponseEntity<String> {
        return ResponseEntity(jubilantTodoTribbleHealth.status.name, jubilantTodoTribbleHealth.status)
    }

    @GetMapping("overview")
    fun getOverview(): ResponseEntity<StatusOverviewResponse> {
        val statusOverview = StatusOverviewResponse()
        statusOverview.services.add(getServiceStatus(jubilantTodoTribbleHealth))
        statusOverview.services.add(getServiceStatus(gitlabHealth))
        return ResponseEntity(statusOverview, HttpStatus.OK)
    }

    private fun getServiceStatus(serviceHealth: ServiceHealth): ServiceStatus {
        val serviceStatus = ServiceStatus()
        serviceStatus.name = serviceHealth.name
        serviceStatus.status = serviceHealth.status.value()
        return serviceStatus
    }
}
