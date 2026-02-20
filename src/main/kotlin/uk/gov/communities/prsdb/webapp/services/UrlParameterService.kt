package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.ServletRequest

abstract class UrlParameterService<T>(
    val request: ServletRequest,
) {
    fun getParameterOrNull(parameterName: String): String? = request.getParameter(parameterName)

    abstract fun getParameterOrNull(): T?

    abstract fun createParameterPair(value: T): Pair<String, String>
}
