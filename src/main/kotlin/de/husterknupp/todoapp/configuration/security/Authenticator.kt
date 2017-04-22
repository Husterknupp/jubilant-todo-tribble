package de.husterknupp.todoapp.configuration.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import de.husterknupp.todoapp.configuration.AccessConfiguration
import de.husterknupp.todoapp.configuration.JwtConfiguration
import de.husterknupp.todoapp.configuration.LdapConfiguration
import de.husterknupp.todoapp.configuration.logger
import de.husterknupp.todoapp.model.AuthenticationResult
import org.springframework.context.annotation.Bean
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.ldap.AuthenticationException
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.core.support.LdapContextSource
import org.springframework.ldap.query.LdapQueryBuilder
import org.springframework.stereotype.Component
import java.util.*
import javax.servlet.http.HttpServletRequest

@Component
open class Authenticator constructor(
        private val ldapAuthenticator: LdapAuthenticator,
        private val jwtAuthenticator: JwtAuthenticator,
        private val accessConfiguration: AccessConfiguration
) {

    fun authenticate(request: HttpServletRequest): AuthenticationResult {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null) {
            val jwtAuthPrefix = "Bearer "
            val basicAuthPrefix = "Basic "
            when {
                authHeader.startsWith(jwtAuthPrefix) -> {
                    val token = authHeader.substring(jwtAuthPrefix.length)
                    return authenticateWithJwt(token)
                }
                authHeader.startsWith(basicAuthPrefix) -> {
                    val hash = authHeader.substring(basicAuthPrefix.length).trim()
                    return authenticateWithLdap(hash)
                }
            }
        }
        return AuthenticationResult()
    }

    private fun authenticateWithJwt(token: String): AuthenticationResult {
        val authenticationResult = AuthenticationResult()
        try {
            val decodedJwt = jwtAuthenticator.validate(token)
            authenticationResult.success = true
            val user = jwtAuthenticator.getUser(decodedJwt)
            authenticationResult.user = user
            val roles = jwtAuthenticator.getRoles(decodedJwt)
            authenticationResult.roles.addAll(roles)
            if (jwtAuthenticator.doesRequireUpdate(decodedJwt)) {
                authenticationResult.jwt = jwtAuthenticator.generate(user, roles)
            } else {
                authenticationResult.jwt = token
            }
        } catch (e: JWTVerificationException) {
            authenticationResult.success = false
        }
        return authenticationResult
    }

    private fun authenticateWithLdap(hash: String): AuthenticationResult {
        val authenticationResult = AuthenticationResult()
        val credentials = Base64.getDecoder().decode(hash)
        val user = String(credentials.takeWhile { it != ':'.toByte() }.toByteArray())
        val password = credentials.takeLastWhile { it != ':'.toByte() }.toByteArray()
        if (ldapAuthenticator.authenticate(user, password)) {
            authenticationResult.success = true
            authenticationResult.user = user
            val groups = ldapAuthenticator.getGroups(user)
            val roles = getRoles(groups)
            authenticationResult.roles.addAll(roles)
            authenticationResult.jwt = jwtAuthenticator.generate(user, roles)
        }
        return authenticationResult
    }

    private fun getRoles(groups: Collection<String>): Collection<String> {
        return accessConfiguration.roles
                .filter { it.value.find { groups.contains(it) } != null }
                .keys
    }
}



@Component
open class LdapAuthenticator constructor(
        private val ldapConfiguration: LdapConfiguration
) {

    private val log by logger()

    @Bean
    open fun ldapTemplate(): LdapTemplate {
        val contextSource = LdapContextSource()
        contextSource.setUrl(ldapConfiguration.url)
        contextSource.setBase(ldapConfiguration.baseDn)
        contextSource.userDn = ldapConfiguration.authDn
        contextSource.password = ldapConfiguration.authPassword
        contextSource.afterPropertiesSet()
        val ldapTemplate = LdapTemplate(contextSource)
        ldapTemplate.afterPropertiesSet()
        return ldapTemplate
    }

    fun authenticate(user: String, password: ByteArray): Boolean {
        try {
            ldapTemplate().authenticate(
                    LdapQueryBuilder.query()
                            .base(ldapConfiguration.usersBaseDn)
                            .where("uid").`is`(user),
                    String(password))
            return true
        } catch(e: AuthenticationException) {
            log.info("Invalid login attempt for user $user: wrong credentials")
        } catch(e: EmptyResultDataAccessException) {
            log.info("Invalid login attempt for user $user: unknown user")
        } catch(e: Exception) {
            log.error("Error while trying to authenticate with LDAP", e)
        }
        return false
    }

    fun getGroups(user: String): List<String> {
        return ldapTemplate().search(
                LdapQueryBuilder.query()
                        .base(ldapConfiguration.groupsBaseDn)
                        .where("memberUid").`is`(user),
                AttributesMapper { it.get("cn")?.get()?.toString() })
                .filter { it != null }.map { it!! }
    }
}

@Component
open class JwtAuthenticator constructor(
        private val jwtConfiguration: JwtConfiguration
){

    @Bean
    open fun jwtVerifier(): JWTVerifier {
        return JWT.require(Algorithm.HMAC256(jwtConfiguration.secret))
                .withIssuer(jwtConfiguration.issuer)
                .build()
    }

    fun validate(token: String): DecodedJWT {
        return jwtVerifier().verify(token)
    }

    fun generate(user: String, roles: Collection<String>): String {
        return JWT.create()
                .withIssuer(jwtConfiguration.issuer)
                .withIssuedAt(Date(System.currentTimeMillis()))
                .withExpiresAt(Date(System.currentTimeMillis() + jwtConfiguration.duration))
                .withSubject(user)
                .withArrayClaim("roles", roles.toTypedArray())
                .sign(Algorithm.HMAC256(jwtConfiguration.secret))
    }

    fun getUser(decodedJwt: DecodedJWT): String {
        return decodedJwt.subject
    }

    fun getRoles(decodedJwt: DecodedJWT): MutableList<String> {
        return decodedJwt.getClaim("roles").asList(String::class.java)
    }

    fun doesRequireUpdate(decodedJwt: DecodedJWT): Boolean {
        return decodedJwt.expiresAt.before(
                Date((System.currentTimeMillis() + jwtConfiguration.duration * 0.2).toLong()))
    }
}
