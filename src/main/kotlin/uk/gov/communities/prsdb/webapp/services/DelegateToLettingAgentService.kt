package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENTS_DELEGATED_THIS_SESSION

@PrsdbWebService
class DelegateToLettingAgentService(
    private val session: HttpSession,
) {
    fun addDelegatedLettingAgentToSession(
        propertyOwnershipId: Long,
        invitedEmailAddress: String,
    ) = session.setAttribute(
        LETTING_AGENTS_DELEGATED_THIS_SESSION,
        getDelegatedLettingAgentsFromSession() + (propertyOwnershipId to invitedEmailAddress),
    )

    @Suppress("UNCHECKED_CAST")
    fun getDelegatedLettingAgentsFromSession(): MutableMap<Long, String> =
        session.getAttribute(LETTING_AGENTS_DELEGATED_THIS_SESSION) as MutableMap<Long, String>?
            ?: mutableMapOf()
}
