package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.ServletRequest
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.RequestScope

@Service
@RequestScope
class UrlParameterService(
    val request: ServletRequest,
) {
    fun getParameterOrNull(parameterName: String): String? = request.getParameter(parameterName)

    fun getIntParameterOrNull(parameterName: String): Int? = request.getParameter(parameterName)?.toIntOrNull()
}
