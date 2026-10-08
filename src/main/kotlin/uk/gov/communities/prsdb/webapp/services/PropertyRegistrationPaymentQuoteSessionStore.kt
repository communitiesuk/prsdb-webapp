package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationPaymentQuote

@PrsdbWebService
class PropertyRegistrationPaymentQuoteSessionStore(
    private val session: HttpSession,
) {
    fun getQuote(journeyId: String): PropertyRegistrationPaymentQuote? {
        val quote = session.getAttribute(quoteAttributeName(journeyId)) ?: return null
        check(quote is PropertyRegistrationPaymentQuote) { "Invalid payment quote in session for journey $journeyId" }
        return quote
    }

    fun storeQuote(
        journeyId: String,
        quote: PropertyRegistrationPaymentQuote,
    ) {
        session.setAttribute(quoteAttributeName(journeyId), quote)
    }

    private fun quoteAttributeName(journeyId: String) = "$QUOTE_SESSION_ATTRIBUTE_PREFIX$journeyId"

    companion object {
        private const val QUOTE_SESSION_ATTRIBUTE_PREFIX = "propertyRegistrationPaymentQuote:"
    }
}
