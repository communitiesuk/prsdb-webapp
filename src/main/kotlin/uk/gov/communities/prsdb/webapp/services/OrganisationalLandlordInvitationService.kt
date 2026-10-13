package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.containsEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.OrganisationalLandlordInvitationEmail
import java.util.UUID

@PrsdbWebService
class OrganisationalLandlordInvitationService(
    private val invitationRepository: OrganisationalLandlordInvitationRepository,
    private val session: HttpSession,
    private val organisationalLandlordUserService: OrganisationalLandlordUserService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val invitationEmailSender: EmailNotificationService<OrganisationalLandlordInvitationEmail>,
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

    fun getPendingInvitationForJourneyIdOrNull(journeyId: String): OrganisationalLandlordInvitation? =
        getInvitationForJourneyIdOrNull(journeyId)
            ?.takeIf { it.status == OrganisationalLandlordInvitationStatus.PENDING }

    @Transactional
    fun createInvitation(
        invitedEmail: String,
        role: OrganisationalLandlordUserRole,
        organisationalLandlord: OrganisationalLandlord,
    ) {
        // Re-check against the database when saving, as another admin may have invited the same email since validation ran
        if (getInvitedAndMemberEmails(organisationalLandlord).containsEmail(invitedEmail)) return

        val invitation =
            OrganisationalLandlordInvitation(
                token = UUID.randomUUID(),
                invitedEmail = invitedEmail,
                organisationalLandlord = organisationalLandlord,
                role = role,
            )
        invitationRepository.save(invitation)

        invitationEmailSender.sendEmail(
            invitedEmail,
            OrganisationalLandlordInvitationEmail(
                organisationName = organisationalLandlord.name,
                invitationUri = absoluteUrlProvider.buildOrganisationalLandlordInvitationUri(invitation.token.toString()),
            ),
        )
    }

    // TODO PDJB-1822: Only count PENDING invitations once the invitation status model is available
    fun getInvitedAndMemberEmails(organisationalLandlord: OrganisationalLandlord): List<String> =
        invitationRepository.findByOrganisationalLandlord(organisationalLandlord).map { it.invitedEmail } +
            organisationalLandlordUserService.getOrganisationalLandlordUsers(organisationalLandlord).map { it.email }

    @Suppress("UNCHECKED_CAST")
    private fun getJourneyIdInvitationTokenPairsFromSession(): MutableList<Pair<String, String>>? =
        session
            .getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS) as? MutableList<Pair<String, String>>
}
