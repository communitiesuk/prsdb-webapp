package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationEmail

@PrsdbWebService
class JointLandlordInvitationEmailSender(
    private val jointLandlordInvitationService: JointLandlordInvitationService,
    private val emailNotificationService: EmailNotificationService<JointLandlordInvitationEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) {
    fun sendInvitationEmails(
        jointLandlordEmails: List<String>,
        propertyOwnership: PropertyOwnership,
    ) {
        val senderName = propertyOwnership.primaryLandlord.name
        val propertyAddress = propertyOwnership.address.singleLineAddress

        jointLandlordEmails.forEach { email ->
            val token = jointLandlordInvitationService.createInvitationToken(email, propertyOwnership)
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
}
