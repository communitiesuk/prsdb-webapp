package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.ServletRequest
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.RequestScope

abstract class UrlParameterService<T>(
    val request: ServletRequest,
) {
    fun getParameterOrNull(parameterName: String): String? = request.getParameter(parameterName)

    abstract fun getParameterOrNull(): T?

    abstract fun getParameterPair(index: T): Pair<String, String>
}

@Service
@RequestScope
class ArrayIndexParameterService(
    request: ServletRequest,
) : UrlParameterService<Int>(request) {
    override fun getParameterOrNull(): Int? = getParameterOrNull(PARAMETER_NAME)?.toIntOrNull()

    override fun getParameterPair(index: Int): Pair<String, String> = PARAMETER_NAME to index.toString()

    companion object {
        private const val PARAMETER_NAME = "index"
    }
}
