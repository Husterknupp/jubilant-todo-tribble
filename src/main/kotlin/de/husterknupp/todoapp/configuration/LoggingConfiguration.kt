package de.husterknupp.todoapp.configuration

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration

@Configuration
open class LoggingConfiguration {

}

fun <T : Any> T.logger(): Lazy<Logger> {
    return lazyOf(LoggerFactory.getLogger(unwrapCompanionClass(this.javaClass).name))
}

private fun <T: Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return if (ofClass.kotlin.isCompanion && ofClass.enclosingClass != null) {
        ofClass.enclosingClass
    } else {
        ofClass
    }
}
