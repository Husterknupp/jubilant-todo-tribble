package de.husterknupp.todoapp.configuration.security

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Configuration
open class BypassAutoSecurityConfiguration: WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity?) {
        http!!.csrf().disable()
    }

    override fun configure(auth: AuthenticationManagerBuilder?) {}
}

@Configuration
open class RequestConfiguration constructor(
        private val requestAuthenticationInterceptor: RequestAuthenticationInterceptor
): WebMvcConfigurerAdapter() {

    override fun addInterceptors(registry: InterceptorRegistry?) {
        registry!!.addInterceptor(requestAuthenticationInterceptor)
    }
}

@Component
open class RequestAuthenticationInterceptor constructor(
        private val authenticator: Authenticator
) : HandlerInterceptorAdapter() {

    override fun preHandle(request: HttpServletRequest?, response: HttpServletResponse?, handler: Any?): Boolean {
        if (request == null || response == null || isAutoAuthDisabled(handler)) {
            return true
        }
        val authenticationResult = authenticator.authenticate(request)
        if (authenticationResult.success) {
            response.setHeader("jwt", authenticationResult.jwt)
        }
        if (hasAccess(authenticationResult.roles, getRequiredRoles(handler))) {
            return true
        } else {
            if (authenticationResult.success) {
                response.status = HttpStatus.FORBIDDEN.value()
            } else {
                response.status = HttpStatus.UNAUTHORIZED.value()
            }
            return false
        }
    }

    private fun isAutoAuthDisabled(handler: Any?): Boolean {
        return (handler != null && handler is HandlerMethod
                && (handler.method.declaringClass.getAnnotation(DisableAutoAuth::class.java) != null
                || handler.method.getAnnotation(DisableAutoAuth::class.java) != null))
    }

    private fun getRequiredRoles(handler: Any?): Collection<String> {
        if (handler != null && handler is HandlerMethod) {
            val required = mutableSetOf<String>()
            required.addAll(handler.method.declaringClass.getAnnotation(Secured::class.java)?.value?.toList()?: emptyList())
            required.addAll(handler.method.getAnnotation(Secured::class.java)?.value?.toList()?: emptyList())
            return required
        }
        return emptyList()
    }

    private fun hasAccess(granted: Collection<String>, required: Collection<String>): Boolean {
        return granted.containsAll(required)
    }
}
