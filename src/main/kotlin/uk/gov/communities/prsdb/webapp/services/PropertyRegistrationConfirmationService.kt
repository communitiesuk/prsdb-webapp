package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTY_FORM_CONTEXTS_DELETED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REGISTRATION
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_NUMBER

@PrsdbWebService
class PropertyRegistrationConfirmationService(
    private val session: HttpSession,
) {
    fun setLastPrnRegisteredThisSession(prn: Long) = session.setAttribute(PROPERTY_REGISTRATION_NUMBER, prn)

    fun getLastPrnRegisteredThisSession() = session.getAttribute(PROPERTY_REGISTRATION_NUMBER)?.toString()?.toLong()

    fun setLandlordReactivatedByLastRegistration(landlordReactivated: Boolean) =
        session.setAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REGISTRATION, landlordReactivated)

    fun wasLandlordReactivatedByLastRegistration(): Boolean =
        session.getAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REGISTRATION) == true

    fun addIncompletePropertyFormContextsDeletedThisSession(formContextId: String) {
        session.setAttribute(
            INCOMPLETE_PROPERTY_FORM_CONTEXTS_DELETED_THIS_SESSION,
            getIncompletePropertyFormContextsDeletedThisSession().plus(formContextId),
        )
    }

    fun wasIncompletePropertyDeletedThisSession(contextId: String): Boolean =
        getIncompletePropertyFormContextsDeletedThisSession().contains(contextId)

    private fun getIncompletePropertyFormContextsDeletedThisSession(): MutableList<String> =
        session.getAttribute(INCOMPLETE_PROPERTY_FORM_CONTEXTS_DELETED_THIS_SESSION) as MutableList<String>? ?: mutableListOf()
}
