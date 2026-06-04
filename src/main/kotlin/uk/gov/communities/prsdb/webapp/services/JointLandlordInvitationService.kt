package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS
import uk.gov.communities.prsdb.webapp.constants.USER_SENT_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationEmail
import java.util.UUID

@PrsdbWebService
class JointLandlordInvitationService(
    val invitationRepository: JointLandlordInvitationRepository,
    private val invitationEmailSender: EmailNotificationService<JointLandlordInvitationEmail>,
    private val confirmationEmailSender: EmailNotificationService<JointLandlordInvitationConfirmationEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val session: HttpSession,
) {
    fun sendInvitationEmails(
        jointLandlordEmails: List<String>,
        propertyOwnership: PropertyOwnership,
        invitingLandlord: Landlord,
    ) {
        val senderName = invitingLandlord.name
        val propertyAddress = propertyOwnership.address.toMultiLineAddress()

        jointLandlordEmails.forEach { email ->
            val token = createInvitationToken(email, propertyOwnership, invitingLandlord)
            val invitationUri = absoluteUrlProvider.buildJointLandlordInvitationUri(token)

            invitationEmailSender.sendEmail(
                email,
                JointLandlordInvitationEmail(
                    senderName = senderName,
                    propertyAddress = propertyAddress,
                    invitationUri = invitationUri,
                ),
            )
        }

        if (jointLandlordEmails.isNotEmpty()) {
            val propertyRecordUrl = absoluteUrlProvider.buildPropertyDetailsUri(propertyOwnership.id).toString()
            confirmationEmailSender.sendEmail(
                invitingLandlord.email,
                JointLandlordInvitationConfirmationEmail(
                    senderName = senderName,
                    propertyAddress = propertyAddress,
                    jointLandlordEmails = jointLandlordEmails,
                    propertyRecordUrl = propertyRecordUrl,
                ),
            )

            // TODO: Send notification email to existing joint landlords on the property
        }
    }

    private fun createInvitationToken(
        email: String,
        propertyOwnership: PropertyOwnership,
        invitingLandlord: Landlord,
    ): String {
        val token = UUID.randomUUID()
        invitationRepository.save(JointLandlordInvitation(token, email, propertyOwnership, invitingLandlord))
        return token.toString()
    }

    fun getJourneyIdInvitationTokenPairsFromSession(): MutableList<Pair<String, String>>? =
        getListOfPairsFromSession(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS)

    fun addJourneyIdInvitationTokenPairToSession(
        journeyId: String,
        token: String,
    ) {
        val existingPairs =
            getListOfPairsFromSession<String, String>(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS)
                ?: mutableListOf()
        existingPairs.add(Pair(journeyId, token))
        session.setAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS, existingPairs)
    }

    fun getInvitationTokenForJourneyIdFromSession(journeyId: String): String? =
        getJourneyIdInvitationTokenPairsFromSession()?.find { it.first == journeyId }?.second

    // TODO PDJB-261 or PDJB-264
    //  Add an internal step before the confirmation page that will delete the invitation from db and remove all journeys with that token from the session
    fun clearJourneyIdInvitationTokenPairsForTokenFromSession(token: String) {
        val remainingPairs = getJourneyIdInvitationTokenPairsFromSession()?.filter { pair -> pair.second != token }
        session.setAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS, remainingPairs)
    }

    fun addOrUpdateUserSentToLandlordRegistrationTaskToSession(
        jointLandlordInvitationJourneyId: String,
        userSentToLandlordRegistration: Boolean,
    ) {
        val existingPairs: MutableList<Pair<String, Boolean>> =
            getListOfPairsFromSession(USER_SENT_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION)
                ?: mutableListOf()
        val existingIndex = existingPairs.indexOfFirst { it.first == jointLandlordInvitationJourneyId }
        if (existingIndex >= 0) {
            existingPairs[existingIndex] = Pair(jointLandlordInvitationJourneyId, userSentToLandlordRegistration)
        } else {
            existingPairs.add(Pair(jointLandlordInvitationJourneyId, userSentToLandlordRegistration))
        }
        session.setAttribute(USER_SENT_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION, existingPairs)
    }

    // TODO PDJB-264 - use this to decide whether to show the success banner
    fun getUserSentToLandlordRegistrationTaskFromSession(jointLandlordInvitationAcceptanceJourneyId: String): Boolean? =
        getListOfPairsFromSession<String, Boolean>(USER_SENT_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION)
            ?.find { it.first == jointLandlordInvitationAcceptanceJourneyId }
            ?.second

    @Suppress("UNCHECKED_CAST")
    private fun <T1, T2> getListOfPairsFromSession(sessionAttributeName: String): MutableList<Pair<T1, T2>>? =
        session.getAttribute(sessionAttributeName) as? MutableList<Pair<T1, T2>>
}
