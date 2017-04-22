package de.husterknupp.todoapp.controller

import org.springframework.boot.autoconfigure.web.ErrorAttributes
import org.springframework.boot.autoconfigure.web.ErrorController
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.ServletRequestAttributes
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private const val ERROR_PATH = "/error"

@RestController
class ErrorJsonController constructor(
        private val errorAttributes: ErrorAttributes
): ErrorController {

    private val PATHS_WITHOUT_FALLBACK: Array<String> = arrayOf(
            "/api/"
    )

    private val DEFAULT_RESOURCE = "public/index.html"

    override fun getErrorPath(): String {
        return ERROR_PATH
    }

    @RequestMapping(ERROR_PATH)
    fun getError(request: HttpServletRequest?, response: HttpServletResponse?): ResponseEntity<Any?> {
        if (response?.status == HttpStatus.NOT_FOUND.value() && shouldFallback(request)) {
            return ResponseEntity(ClassPathResource(DEFAULT_RESOURCE), HttpStatus.OK)
        }
        val errorAttributes = errorAttributes.getErrorAttributes(ServletRequestAttributes(request), true)
        return ResponseEntity(errorAttributes, HttpStatus.valueOf(errorAttributes.get("status") as? Int?: 500))
    }

    private fun shouldFallback(request: HttpServletRequest?): Boolean {
        val url = request?.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI).toString()
        PATHS_WITHOUT_FALLBACK.forEach {
            if (url.contains(it, true)) {
                return false
            }
        }
        return true
    }
}
