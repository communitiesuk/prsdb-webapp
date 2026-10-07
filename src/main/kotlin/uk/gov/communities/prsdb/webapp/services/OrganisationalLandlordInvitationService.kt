package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import java.util.UUID

@PrsdbWebService
class OrganisationalLandlordInvitationService(
    private val invitationRepository: OrganisationalLandlordInvitationRepository,
    private val session: HttpSession,
    private val organisationalLandlordUserService: OrganisationalLandlordUserService,
    private val prsdbUserService: PrsdbUserService,
) {
    fun addJourneyIdInvitationTokenPairToSession(
        journeyId: String,
        token: String,
    ) {
        val existingPairs = getJourneyIdInvitationTokenPairsFromSession() ?: mutableListOf()
        existingPairs.add(journeyId to token)
        session.setAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS, existingPairs)
    }

    fun clearJourneyIdInvitationTokenPairsForTokenFromSession(token: String) {
        val remainingPairs = getJourneyIdInvitationTokenPairsFromSession()?.filter { pair -> pair.second != token }
        session.setAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS, remainingPairs)
    }

    @Transactional
    fun acceptInvitation(
        journeyId: String,
        baseUserId: String,
        name: String,
        email: String,
    ): String {
        val invitation =
            getInvitationForJourneyIdOrNull(journeyId)
                ?: throw PrsdbWebException(
                    "Could not find an organisational landlord invitation associated with journey $journeyId",
                )

        val baseUser = prsdbUserService.findOrCreatePrsdbUser(baseUserId)
        organisationalLandlordUserService.createOrganisationalLandlordUser(
            organisationalLandlord = invitation.organisationalLandlord,
            baseUser = baseUser,
            name = name,
            email = email,
            role = invitation.role,
        )
        invitationRepository.delete(invitation)

        return invitation.token.toString()
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

    @Suppress("UNCHECKED_CAST")
    private fun getJourneyIdInvitationTokenPairsFromSession(): MutableList<Pair<String, String>>? =
        session
            .getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS) as? MutableList<Pair<String, String>>
}
