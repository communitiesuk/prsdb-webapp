package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.entity.LettingAgentAccess
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.LettingAgentAccessRepository
import java.util.UUID

@Service
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

    fun getInvitationByTokenOrNull(token: UUID): LettingAgentAccess? = lettingAgentAccessRepository.findByToken(token)

    fun getInvitationByToken(token: UUID): LettingAgentAccess =
        getInvitationByTokenOrNull(token)
            ?: throw EntityNotFoundException("No letting agent access found for token $token")

    fun getTokenIsValid(token: String): Boolean {
        val uuid =
            try {
                UUID.fromString(token)
            } catch (_: IllegalArgumentException) {
                return false
            }
        return lettingAgentAccessRepository.findByToken(uuid) != null
    }

    @Transactional
    fun deleteDelegationByPropertyOwnershipId(propertyOwnershipId: Long) {
        lettingAgentAccessRepository.deleteByPropertyOwnershipId(propertyOwnershipId)
    }
}
