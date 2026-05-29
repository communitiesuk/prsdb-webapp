package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_REGISTRATION_JOURNEY_ID_FOR_JOINT_LANDLORD_INVITATION_JOURNEY
import uk.gov.communities.prsdb.webapp.constants.USER_DIRECTED_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationEmail
import java.util.UUID

@PrsdbWebService
class JointLandlordInvitationService(
    val invitationRepository: JointLandlordInvitationRepository,
    private val emailNotificationService: EmailNotificationService<JointLandlordInvitationEmail>,
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

            emailNotificationService.sendEmail(
                email,
                JointLandlordInvitationEmail(
                    senderName = senderName,
                    propertyAddress = propertyAddress,
                    invitationUri = invitationUri,
                ),
            )
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
    ) = addToListOfPairsInSession(
        JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS,
        journeyId,
        token,
    )

    fun getInvitationTokenForJourneyIdFromSession(journeyId: String): String? =
        getJourneyIdInvitationTokenPairsFromSession()?.find { it.first == journeyId }?.second

    // TODO PDJB-261 or PDJB-264
    //  Add an internal step before the confirmation page that will delete the invitation from db and remove all journeys with that token from the session
    fun clearJourneyIdInvitationTokenPairsForTokenFromSession(token: String) {
        val remainingPairs = getJourneyIdInvitationTokenPairsFromSession()?.filter { pair -> pair.second != token }
        session.setAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS, remainingPairs)
    }

    fun addLandlordRegistrationAndAcceptanceJourneyIdPairsToSession(
        landlordRegistrationJourneyId: String,
        jointLandlordInvitationAcceptanceJourneyId: String,
    ) = addToListOfPairsInSession(
        LANDLORD_REGISTRATION_JOURNEY_ID_FOR_JOINT_LANDLORD_INVITATION_JOURNEY,
        landlordRegistrationJourneyId,
        jointLandlordInvitationAcceptanceJourneyId,
    )

    fun getLandlordRegistrationJourneyIdForAcceptanceJourneyIdFromSession(jointLandlordInvitationAcceptanceJourneyId: String): String? =
        getListOfPairsFromSession<String, String>(LANDLORD_REGISTRATION_JOURNEY_ID_FOR_JOINT_LANDLORD_INVITATION_JOURNEY)
            ?.find { it.second == jointLandlordInvitationAcceptanceJourneyId }
            ?.first

    fun addUserSentToLandlordRegistrationWhileAcceptingJointLandlordInvitationToSession(
        jointLandlordInvitationJourneyId: String,
        userSentToLandlordRegistration: Boolean,
    ) = addOrUpdateInListOfPairsInSession(
        USER_DIRECTED_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION,
        jointLandlordInvitationJourneyId,
        userSentToLandlordRegistration,
    )

    fun getJointLandlordInvitationJourneyIdWhereUserWasSentToLandlordRegistrationFromSession() =
        getListOfPairsFromSession<String, Boolean>(USER_DIRECTED_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION)
            ?.find { it.second }
            ?.first

    // TODO PDJB-264 - use this to decide whether to show the success banner
    fun getUserSentToLandlordRegistrationWhileAcceptingThisJointLandlordInvitationFromSession(
        jointLandlordInvitationAcceptanceJourneyId: String,
    ): Boolean? =
        getListOfPairsFromSession<String, Boolean>(USER_DIRECTED_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION)
            ?.find { it.first == jointLandlordInvitationAcceptanceJourneyId }
            ?.second

    private fun <T1, T2> addToListOfPairsInSession(
        sessionAttributeName: String,
        firstValue: T1,
        secondValue: T2,
    ) {
        val existingPairs: MutableList<Pair<T1, T2>> = getListOfPairsFromSession(sessionAttributeName) ?: mutableListOf()
        existingPairs.add(Pair(firstValue, secondValue))
        session.setAttribute(sessionAttributeName, existingPairs)
    }

    private fun <T1, T2> addOrUpdateInListOfPairsInSession(
        sessionAttributeName: String,
        firstValue: T1,
        secondValue: T2,
    ) {
        val existingPairs: MutableList<Pair<T1, T2>> = getListOfPairsFromSession(sessionAttributeName) ?: mutableListOf()
        val existingIndex = existingPairs.indexOfFirst { it.first == firstValue }
        if (existingIndex >= 0) {
            existingPairs[existingIndex] = Pair(firstValue, secondValue)
        } else {
            existingPairs.add(Pair(firstValue, secondValue))
        }
        session.setAttribute(sessionAttributeName, existingPairs)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T1, T2> getListOfPairsFromSession(sessionAttributeName: String): MutableList<Pair<T1, T2>>? =
        session.getAttribute(sessionAttributeName) as? MutableList<Pair<T1, T2>>
}
