package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.LettingAgentAccess
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.LettingAgentAccessRepository
import java.util.UUID

@PrsdbWebService
class LettingAgentAccessService(
    private val lettingAgentAccessRepository: LettingAgentAccessRepository,
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
    fun deleteInvitation(lettingAgentAccess: LettingAgentAccess) {
        lettingAgentAccessRepository.delete(lettingAgentAccess)
    }

    @Transactional
    fun deleteInvitationByPropertyOwnershipId(propertyOwnershipId: Long) {
        val invitation = lettingAgentAccessRepository.findByPropertyOwnershipId(propertyOwnershipId) ?: return
        lettingAgentAccessRepository.delete(invitation)
    }
}
