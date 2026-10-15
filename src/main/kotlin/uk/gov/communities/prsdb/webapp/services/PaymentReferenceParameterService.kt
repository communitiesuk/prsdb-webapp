package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.ServletRequest
import org.springframework.web.context.annotation.RequestScope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService

@PrsdbWebService
@RequestScope
class PaymentReferenceParameterService(
    request: ServletRequest,
) : UrlParameterService<String>(request) {
    override fun getParameterOrNull(): String? = getParameterOrNull(PARAMETER_NAME)

    override fun createParameterPair(value: String): Pair<String, String> = PARAMETER_NAME to value

    companion object {
        const val PARAMETER_NAME = "paymentReference"
    }
}
