package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.CancelDelegationJointLandlordNotificationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.CancelDelegationLandlordConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.CancelDelegationLettingAgentNotificationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.DelegateToLettingAgentConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.DelegateToLettingAgentInvitationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.DelegateToLettingAgentInvitationWithDeadlineEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordDelegateToLettingAgentNotificationEmail
import java.util.UUID

@PrsdbWebService
class DelegateToLettingAgentEmailService(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val userToLandlordService: UserToLandlordService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val confirmationEmailService: EmailNotificationService<DelegateToLettingAgentConfirmationEmail>,
    private val notificationEmailService: EmailNotificationService<JointLandlordDelegateToLettingAgentNotificationEmail>,
    private val cancelLandlordConfirmationEmailService: EmailNotificationService<CancelDelegationLandlordConfirmationEmail>,
    private val cancelJointLandlordNotificationEmailService: EmailNotificationService<CancelDelegationJointLandlordNotificationEmail>,
    private val cancelLettingAgentNotificationEmailService: EmailNotificationService<CancelDelegationLettingAgentNotificationEmail>,
    private val invitationEmailService: EmailNotificationService<DelegateToLettingAgentInvitationEmail>,
    private val invitationWithDeadlineEmailService: EmailNotificationService<DelegateToLettingAgentInvitationWithDeadlineEmail>,
    private val currentUserService: CurrentUserService,
) {
    fun sendDelegationEmailToLandlords(
        propertyOwnershipId: Long,
        invitedLettingAgentEmail: String,
    ) {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)
        val actingLandlord = userToLandlordService.getCurrentLandlordForUser()
        val currentUser = currentUserService.getRegisteredUserDetails()
        val propertyRecordUrl = absoluteUrlProvider.buildPropertyDetailsUri(propertyOwnership.id).toString()

        confirmationEmailService.sendEmail(
            currentUser.email,
            DelegateToLettingAgentConfirmationEmail(
                recipientName = currentUser.name,
                propertyAddress = propertyOwnership.address.toMultiLineAddress(),
                lettingAgentEmail = invitedLettingAgentEmail,
                propertyRecordUrl = propertyRecordUrl,
            ),
        )
        // TODO: PDJB-1274: Update emails to account for other users in the org

        propertyOwnership.otherLandlordsTo(actingLandlord).forEach { landlord ->
            notificationEmailService.sendEmailToLandlord(
                landlord,
                JointLandlordDelegateToLettingAgentNotificationEmail(
                    recipientName = landlord.name,
                    propertyAddress = propertyOwnership.address.toMultiLineAddress(),
                    lettingAgentEmail = invitedLettingAgentEmail,
                    propertyRecordUrl = propertyRecordUrl,
                ),
            )
        }
    }

    fun sendDelegationEmailToLettingAgent(
        propertyOwnership: PropertyOwnership,
        landlordName: String,
        lettingAgentEmail: String,
        deadlineDate: String? = null,
        invitationToken: UUID,
    ) {
        val invitationLink = absoluteUrlProvider.buildLettingAgentPropertyDetailsUri(invitationToken).toString()

        if (deadlineDate != null) {
            invitationWithDeadlineEmailService.sendEmail(
                lettingAgentEmail,
                DelegateToLettingAgentInvitationWithDeadlineEmail(
                    landlordName = landlordName,
                    propertyAddress = propertyOwnership.address.toMultiLineAddress(),
                    invitationLink = invitationLink,
                    deadlineDate = deadlineDate,
                    singleLineAddress = propertyOwnership.address.singleLineAddress,
                ),
            )
        } else {
            invitationEmailService.sendEmail(
                lettingAgentEmail,
                DelegateToLettingAgentInvitationEmail(
                    landlordName = landlordName,
                    propertyAddress = propertyOwnership.address.toMultiLineAddress(),
                    invitationLink = invitationLink,
                    singleLineAddress = propertyOwnership.address.singleLineAddress,
                ),
            )
        }
    }

    fun sendCancellationEmails(
        propertyOwnership: PropertyOwnership,
        lettingAgentEmail: String,
    ) {
        val actingLandlord = userToLandlordService.getCurrentLandlordForUser()
        val currentUser = currentUserService.getRegisteredUserDetails()
        val propertyAddress = propertyOwnership.address.toMultiLineAddress()
        val propertyRecordUrl = absoluteUrlProvider.buildPropertyDetailsUri(propertyOwnership.id).toString()

        cancelLandlordConfirmationEmailService.sendEmail(
            currentUser.email,
            CancelDelegationLandlordConfirmationEmail(
                landlordName = currentUser.name,
                propertyAddress = propertyAddress,
                lettingAgentEmail = lettingAgentEmail,
                propertyRecordUrl = propertyRecordUrl,
            ),
        )
        // TODO: PDJB-1274: Update emails to account for other users in the org

        propertyOwnership.otherLandlordsTo(actingLandlord).forEach { jointLandlord ->
            cancelJointLandlordNotificationEmailService.sendEmailToLandlord(
                jointLandlord,
                CancelDelegationJointLandlordNotificationEmail(
                    jointLandlordName = jointLandlord.name,
                    propertyAddress = propertyAddress,
                    lettingAgentEmail = lettingAgentEmail,
                    propertyRecordUrl = propertyRecordUrl,
                ),
            )
        }

        sendLettingAgentCancellationEmail(propertyOwnership, lettingAgentEmail)
    }

    fun sendLettingAgentCancellationEmail(
        propertyOwnership: PropertyOwnership,
        lettingAgentEmail: String,
    ) {
        cancelLettingAgentNotificationEmailService.sendEmail(
            lettingAgentEmail,
            CancelDelegationLettingAgentNotificationEmail(
                propertyAddress = propertyOwnership.address.toMultiLineAddress(),
                singleLineAddress = propertyOwnership.address.singleLineAddress,
            ),
        )
    }
}
