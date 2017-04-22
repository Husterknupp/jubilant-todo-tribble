package de.husterknupp.todoapp.health

import de.husterknupp.todoapp.gateway.GitlabGateway
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.actuate.health.Status
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
private const val TIME_TO_LIVE = 30000L

@Service
open class JubilantTodoTribbleHealth constructor(
        private val applicationContext: ApplicationContext
): ServiceHealth("Jubilant Todo Tribble") {

    init { }

    private val IGNORED_HEALTH_CHECKS: Array<String> = arrayOf(
            "ldapHealthIndicator"
    )

    @Scheduled(fixedDelay = TIME_TO_LIVE)
    fun update() {
        var isHealthy: Boolean = true
        try {
            val healthIndicators = applicationContext.getBeansOfType(HealthIndicator::class.java)
            healthIndicators.forEach {
                if (!IGNORED_HEALTH_CHECKS.contains(it.key) && it.value.health().status != Status.UP) {
                    isHealthy = false
                }
            }
        } catch (e: Exception) {
            isHealthy = false
        }
        status = if (isHealthy) HttpStatus.OK else HttpStatus.SERVICE_UNAVAILABLE
    }
}

@Service
open class GitlabHealth constructor(
        private val gitlabGateway: GitlabGateway
): ServiceHealth("Gitlab") {

    @Scheduled(fixedDelay = TIME_TO_LIVE)
    fun update() {
        status = gitlabGateway.getStatus()
    }
}

sealed class ServiceHealth constructor(
        val name: String
) {
    var status = HttpStatus.OK
        protected set
}
