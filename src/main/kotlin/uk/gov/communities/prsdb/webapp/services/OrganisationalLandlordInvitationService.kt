package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.ACCEPTED_ORGANISATIONAL_LANDLORD_INVITATION_EMAIL
import uk.gov.communities.prsdb.webapp.constants.ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.helpers.EmailHelper.Companion.sendEmailAndLogFailures
import uk.gov.communities.prsdb.webapp.helpers.TransactionHelper.Companion.runAfterTransactionCommits
import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.containsEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.OrganisationalLandlordInvitationAcceptedEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.OrganisationalLandlordInvitationEmail
import java.util.UUID

@PrsdbWebService
class OrganisationalLandlordInvitationService(
    private val invitationRepository: OrganisationalLandlordInvitationRepository,
    private val session: HttpSession,
    private val organisationalLandlordUserService: OrganisationalLandlordUserService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val invitationEmailSender: EmailNotificationService<OrganisationalLandlordInvitationEmail>,
    private val acceptedEmailSender: EmailNotificationService<OrganisationalLandlordInvitationAcceptedEmail>,
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

    fun storeAcceptedInvitationEmailInSession(email: String) {
        session.setAttribute(ACCEPTED_ORGANISATIONAL_LANDLORD_INVITATION_EMAIL, email)
    }

    fun getAcceptedInvitationEmailFromSession(): String? =
        session.getAttribute(ACCEPTED_ORGANISATIONAL_LANDLORD_INVITATION_EMAIL) as? String

    @Transactional
    fun acceptInvitation(
        journeyId: String,
        baseUserId: String,
        name: String,
        email: String,
    ): String {
        val invitation =
            getPendingInvitationForJourneyIdOrNull(journeyId)
                ?: throw PrsdbWebException(
                    "Could not find a pending organisational landlord invitation associated with journey $journeyId",
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

        val isAdmin = invitation.role == OrganisationalLandlordUserRole.ADMIN
        val confirmationEmail =
            OrganisationalLandlordInvitationAcceptedEmail(
                recipientName = name,
                roleName = if (isAdmin) "administrator" else "editor",
                organisationName = invitation.organisationalLandlord.name,
                isAdmin = isAdmin,
                dashboardUrl = absoluteUrlProvider.buildLandlordDashboardUri(),
            )
        runAfterTransactionCommits {
            sendEmailAndLogFailures("the organisational landlord invitation acceptance confirmation email") {
                acceptedEmailSender.sendEmail(email, confirmationEmail)
            }
        }

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
        if (getPendingInvitedAndMemberEmails(organisationalLandlord).containsEmail(invitedEmail)) return

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

    // Expired and hidden invitations can no longer be accepted, so they must not block re-inviting that email address
    fun getPendingInvitedAndMemberEmails(organisationalLandlord: OrganisationalLandlord): List<String> =
        invitationRepository
            .findByOrganisationalLandlord(organisationalLandlord)
            .filter { it.status == OrganisationalLandlordInvitationStatus.PENDING }
            .map { it.invitedEmail } +
            organisationalLandlordUserService.getOrganisationalLandlordUsers(organisationalLandlord).map { it.email }

    @Suppress("UNCHECKED_CAST")
    private fun getJourneyIdInvitationTokenPairsFromSession(): MutableList<Pair<String, String>>? =
        session
            .getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS) as? MutableList<Pair<String, String>>
}
