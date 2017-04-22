package de.husterknupp.todoapp.controller

import de.husterknupp.todoapp.configuration.security.Authenticator
import de.husterknupp.todoapp.configuration.security.DisableAutoAuth
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api/auth/")
open class AuthenticationController constructor(
        private val authenticator: Authenticator
){

    @GetMapping("")
    @DisableAutoAuth
    fun authenticate(request: HttpServletRequest?, response: HttpServletResponse?) {
        if (request == null || response == null) {
            return
        }
        val authenticationResult = authenticator.authenticate(request)
        if (authenticationResult.success) {
            response.setHeader("jwt", authenticationResult.jwt)
            response.status = HttpStatus.OK.value()
        } else {
            response.status = HttpStatus.UNAUTHORIZED.value()
        }
    }

    @GetMapping("administrate")
    @Secured("admin")
    fun administrate() {}

    @GetMapping("view")
    @Secured("agent")
    fun view() {}
}
