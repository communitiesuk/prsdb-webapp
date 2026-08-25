package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION
import uk.gov.communities.prsdb.webapp.database.entity.LettingAgentAccess
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.LettingAgentAccessRepository
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

    fun getInvitationByToken(token: UUID): LettingAgentAccess =
        lettingAgentAccessRepository.findByToken(token)
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
}
