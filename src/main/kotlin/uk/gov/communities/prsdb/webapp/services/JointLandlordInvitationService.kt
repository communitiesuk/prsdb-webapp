package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_TOKEN
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
    fun getPendingAndExpiredInvitations(
        propertyOwnership: PropertyOwnership,
    ): Pair<List<JointLandlordInvitation>, List<JointLandlordInvitation>> {
        val (expired, pending) =
            invitationRepository
                .findByRegisteredOwnership(propertyOwnership)
                .sortedByDescending { it.createdDate }
                .partition { it.isExpired }
        return Pair(pending, expired) // flips the above pair from expired, pending to pending, expired
    }

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

    fun storeTokenInSession(token: String) = session.setAttribute(JOINT_LANDLORD_INVITATION_TOKEN, token)

    fun getTokenFromSession(): String? = session.getAttribute(JOINT_LANDLORD_INVITATION_TOKEN) as String?

    fun clearTokenFromSession() = session.removeAttribute(JOINT_LANDLORD_INVITATION_TOKEN)
}
