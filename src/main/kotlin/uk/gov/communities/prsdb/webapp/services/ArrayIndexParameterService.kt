package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.ServletRequest
import org.springframework.web.context.annotation.RequestScope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService

@PrsdbWebService
@RequestScope
class ArrayIndexParameterService(
    request: ServletRequest,
) : UrlParameterService<Int>(request) {
    override fun getParameterOrNull(): Int? = getParameterOrNull(PARAMETER_NAME)?.toIntOrNull()

    override fun createParameterPair(index: Int): Pair<String, String> = PARAMETER_NAME to index.toString()

    companion object {
        private const val PARAMETER_NAME = "index"
    }
}
