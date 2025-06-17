package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap

@PrsdbWebService
class RegisteredAddressCache(
    private val session: HttpSession,
) {
    fun getCachedAddressRegisteredResult(uprn: Long): Boolean? {
        val cachedResults = objectToStringKeyedMap(session.getAttribute("addressRegisteredResults")) ?: mapOf()
        return cachedResults[uprn.toString()].toString().toBooleanStrictOrNull()
    }

    fun setCachedAddressRegisteredResult(
        uprn: Long,
        result: Boolean,
    ) {
        val cachedResults = objectToStringKeyedMap(session.getAttribute("addressRegisteredResults")) ?: mapOf()

        val newCachedResult = cachedResults + (uprn.toString() to result)

        session.setAttribute("addressRegisteredResults", newCachedResult)
    }
}
