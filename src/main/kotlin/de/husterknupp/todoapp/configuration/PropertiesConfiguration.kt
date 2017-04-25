package de.husterknupp.todoapp.configuration

import de.husterknupp.todoapp.model.BuildInfo
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import javax.validation.constraints.NotNull

@Configuration
@ConfigurationProperties(prefix = "info")
open class InfoConfiguration {

    var build = BuildInfo()

    @NotNull
    lateinit var userAgent: String
}

@Configuration
@ConfigurationProperties(prefix = "gitlab")
open class GitlabConfiguration {

    @NotNull
    lateinit var url: String

    @NotNull
    lateinit var privateToken: String
}

@Configuration
@ConfigurationProperties(prefix = "jira")
open class JiraConfiguration {

    @NotNull
    lateinit var url: String

    @NotNull
    lateinit var username: String

    @NotNull
    lateinit var password: String

    @NotNull
    lateinit var assignee: String
}

@Configuration
@ConfigurationProperties(prefix = "ldap")
open class LdapConfiguration {

    @NotNull
    lateinit var url: String

    @NotNull
    lateinit var baseDn: String

    @NotNull
    lateinit var usersBaseDn: String

    @NotNull
    lateinit var groupsBaseDn: String

    @NotNull
    lateinit var authDn: String

    @NotNull
    lateinit var authPassword: String
}

@Configuration
@ConfigurationProperties(prefix = "jwt")
open class JwtConfiguration {

    @NotNull
    lateinit var issuer: String

    @NotNull
    lateinit var secret: String

    var duration: Long = 0
}

@Configuration
@ConfigurationProperties(prefix = "access")
open class AccessConfiguration {

    var roles: Map<String, Collection<String>> = mutableMapOf()
}

@Configuration
@ConfigurationProperties(prefix = "rest")
open class RestRequestConfiguration {

    var connectionTimeout: Int = -1

    var readTimeout: Int = -1
}
