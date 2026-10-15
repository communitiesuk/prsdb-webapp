package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import java.util.UUID

@PrsdbWebService
class OrganisationalLandlordInvitationService(
    private val invitationRepository: OrganisationalLandlordInvitationRepository,
    private val session: HttpSession,
) {
    // TODO PDJB-1774: Add clearJourneyIdInvitationTokenPairsForTokenFromSession and call it when the invitation journey completes.
    fun addJourneyIdInvitationTokenPairToSession(
        journeyId: String,
        token: String,
    ) {
        val existingPairs = getJourneyIdInvitationTokenPairsFromSession() ?: mutableListOf()
        existingPairs.add(journeyId to token)
        session.setAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS, existingPairs)
    }

    fun getInvitationForJourneyIdOrNull(journeyId: String): OrganisationalLandlordInvitation? {
        val token =
            getJourneyIdInvitationTokenPairsFromSession()
                ?.find { it.first == journeyId }
                ?.second
                ?: return null

        val tokenUuid =
            try {
                UUID.fromString(token)
            } catch (_: IllegalArgumentException) {
                return null
            }

        return invitationRepository.findByToken(tokenUuid)
    }

    fun getInvitationsForOrganisation(organisationalLandlord: OrganisationalLandlord): List<OrganisationalLandlordInvitation> =
        invitationRepository.findByOrganisationalLandlord(organisationalLandlord)

    @Transactional
    fun hideExpiredInvitation(
        invitationId: Long,
        organisationalLandlord: OrganisationalLandlord,
    ) {
        val invitation =
            invitationRepository.findById(invitationId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation with id $invitationId was not found")
            }

        if (invitation.organisationalLandlord.id != organisationalLandlord.id) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Invitation does not belong to this organisation")
        }

        if (invitation.status != OrganisationalLandlordInvitationStatus.EXPIRED) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Only expired invitations can be hidden")
        }

        invitation.isHidden = true
        invitationRepository.save(invitation)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getJourneyIdInvitationTokenPairsFromSession(): MutableList<Pair<String, String>>? =
        session
            .getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS) as? MutableList<Pair<String, String>>
}
