package de.husterknupp.todoapp

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

fun main(args: Array<String>) {
    SpringApplication.run(CustomerServiceCockpitApplication::class.java, *args)
}

@SpringBootApplication
@EnableScheduling
@EnableWebSecurity
open class CustomerServiceCockpitApplication
