package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENT_INVITATION_TOKEN_WITH_JOURNEY_IDS
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION
import uk.gov.communities.prsdb.webapp.database.entity.LettingAgentAccess
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.LettingAgentAccessRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import java.util.UUID

@PrsdbWebService
class LettingAgentAccessService(
    private val lettingAgentAccessRepository: LettingAgentAccessRepository,
    private val session: HttpSession,
) {
    @Transactional
    fun createInvitation(
        propertyOwnership: PropertyOwnership,
        invitedEmail: String,
    ): LettingAgentAccess {
        val token = UUID.randomUUID()
        val lettingAgentAccess = LettingAgentAccess(token, invitedEmail, propertyOwnership)
        return lettingAgentAccessRepository.save(lettingAgentAccess)
    }

    fun getInvitationByTokenOrNull(token: UUID): LettingAgentAccess? = lettingAgentAccessRepository.findByToken(token)

    fun getInvitationByToken(token: UUID): LettingAgentAccess =
        getInvitationByTokenOrNull(token)
            ?: throw EntityNotFoundException("No letting agent access found for token $token")

    fun getInvitationByPropertyOwnershipId(propertyOwnershipId: Long): LettingAgentAccess? =
        lettingAgentAccessRepository.findByPropertyOwnershipId(propertyOwnershipId)

    @Transactional
    fun deleteDelegationByPropertyOwnershipId(propertyOwnershipId: Long) {
        lettingAgentAccessRepository.deleteByPropertyOwnershipId(propertyOwnershipId)
    }

    fun addDelegatedPropertyOwnershipToSession(
        propertyOwnershipId: Long,
        invitedEmail: String,
    ) = session.setAttribute(
        PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION,
        getDelegatedPropertyOwnershipEmailsFromSession() + (propertyOwnershipId to invitedEmail),
    )

    @Suppress("UNCHECKED_CAST")
    fun getDelegatedPropertyOwnershipEmailsFromSession(): MutableMap<Long, String> =
        session.getAttribute(PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION) as MutableMap<Long, String>?
            ?: mutableMapOf()

    fun addRemovedLettingAgentToSession(
        propertyOwnershipId: Long,
        lettingAgentEmail: String,
    ) = session.setAttribute(
        LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS,
        getRemovedLettingAgentsFromSession() + (propertyOwnershipId to lettingAgentEmail),
    )

    fun wasLettingAgentRemovedInThisSession(propertyOwnershipId: Long): Boolean =
        propertyOwnershipId in getRemovedLettingAgentsFromSession()

    fun getRemovedLettingAgentEmailFromSession(propertyOwnershipId: Long): String? =
        getRemovedLettingAgentsFromSession()[propertyOwnershipId]

    @Suppress("UNCHECKED_CAST")
    private fun getRemovedLettingAgentsFromSession(): Map<Long, String> =
        session.getAttribute(LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS) as Map<Long, String>?
            ?: emptyMap()

    fun addJourneyIdInvitationTokenPairToSession(
        journeyId: String,
        token: String,
    ) {
        val existingPairs =
            getJourneyIdInvitationTokenPairsFromSession() ?: mutableListOf()
        existingPairs.add(Pair(journeyId, token))
        session.setAttribute(LETTING_AGENT_INVITATION_TOKEN_WITH_JOURNEY_IDS, existingPairs)
    }

    fun getInvitationTokenForJourneyIdFromSession(journeyId: String): String =
        getJourneyIdInvitationTokenPairsFromSession()?.find { it.first == journeyId }?.second
            ?: throw PrsdbWebException("Invitation token not found in session for journey $journeyId")

    fun getTokenIsValid(token: String): Boolean {
        val tokenUuid =
            try {
                UUID.fromString(token)
            } catch (_: IllegalArgumentException) {
                return false
            }
        return lettingAgentAccessRepository.findByToken(tokenUuid) != null
    }

    @Suppress("UNCHECKED_CAST")
    private fun getJourneyIdInvitationTokenPairsFromSession(): MutableList<Pair<String, String>>? =
        session.getAttribute(LETTING_AGENT_INVITATION_TOKEN_WITH_JOURNEY_IDS) as? MutableList<Pair<String, String>>
}
