package de.husterknupp.todoapp.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Configuration
open class RestConfiguration constructor(
        private val requestConfiguration: RestRequestConfiguration,
        private val userAgentInterceptor: UserAgentInterceptor
) {

    @Bean
    open fun restTemplate(): RestTemplate {
        val requestFactory = HttpComponentsClientHttpRequestFactory()
        requestFactory.setConnectTimeout(requestConfiguration.connectionTimeout)
        requestFactory.setReadTimeout(requestConfiguration.readTimeout)
        val restTemplate = RestTemplate(requestFactory)
        restTemplate.interceptors.add(userAgentInterceptor)
        return restTemplate
    }
}

@Component
open class UserAgentInterceptor constructor(
        private val infoConfiguration: InfoConfiguration
): ClientHttpRequestInterceptor {

    override fun intercept(request: HttpRequest?, body: ByteArray?, execution: ClientHttpRequestExecution?): ClientHttpResponse {
        request!!.headers.add(HttpHeaders.USER_AGENT, infoConfiguration.userAgent)
        return execution!!.execute(request, body)
    }
}
