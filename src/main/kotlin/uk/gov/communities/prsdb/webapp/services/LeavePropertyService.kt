package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_LEFT_THIS_SESSION
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.NotificationHelper
import uk.gov.communities.prsdb.webapp.helpers.TransactionHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordYouLeftConfirmation
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordYouLeftConfirmationAccountDeactivated

@PrsdbWebService
class LeavePropertyService(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val session: HttpSession,
    private val confirmationEmailSender: EmailNotificationService<JointLandlordYouLeftConfirmation>,
    private val confirmationAccountDeactivatedEmailSender: EmailNotificationService<JointLandlordYouLeftConfirmationAccountDeactivated>,
    private val swapToIndividualNudgeEmailService: SwapToIndividualNudgeEmailService,
    private val userToLandlordService: UserToLandlordService,
    private val currentUserService: CurrentUserService,
    private val deactivationService: DeactivationService,
    private val featureFlagManager: FeatureFlagManager,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) {
    fun getPropertyOwnershipIfUserCanLeave(propertyOwnershipId: Long): PropertyOwnership {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)
        val currentLandlord = userToLandlordService.getCurrentLandlordForUser()
        val isLandlordOnProperty =
            propertyOwnership
                .landlords
                .any { it.id == currentLandlord.id }
        val isJointlyOwned = propertyOwnership.landlords.size >= 2
        if (!isLandlordOnProperty || !isJointlyOwned) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Landlord ${currentLandlord.id} is not authorised to leave property ownership $propertyOwnershipId",
            )
        }
        return propertyOwnership
    }

    @Transactional
    fun leavePropertyOwnership(
        landlord: Landlord,
        propertyOwnership: PropertyOwnership,
    ) {
        val currentUser = currentUserService.getRegisteredUserDetails()

        propertyOwnershipService.removeLandlord(propertyOwnership, landlord)
        deactivationService.deactivateLandlordIfNoActiveProperties(landlord)
        val landlordWasDeactivated = !landlord.isActive

        TransactionHelper.runAfterTransactionCommits {
            NotificationHelper.sendNotificationAndLogFailures(
                "the leave property confirmation email for property ownership ${propertyOwnership.id}",
            ) {
                // TODO PDJB-2149: Remove the feature flag check when ACTIVE_INACTIVE is removed
                if (landlordWasDeactivated && featureFlagManager.checkFeature(ACTIVE_INACTIVE)) {
                    confirmationAccountDeactivatedEmailSender.sendEmail(
                        currentUser.email,
                        JointLandlordYouLeftConfirmationAccountDeactivated(
                            recipientName = currentUser.name,
                            propertyAddress = propertyOwnership.address.toMultiLineAddress(),
                            dashboardUrl = absoluteUrlProvider.buildLandlordDashboardUri().toString(),
                        ),
                    )
                } else {
                    confirmationEmailSender.sendEmail(
                        currentUser.email,
                        JointLandlordYouLeftConfirmation(
                            recipientName = currentUser.name,
                            propertyAddress = propertyOwnership.address.toMultiLineAddress(),
                        ),
                    )
                }
            }
            // TODO: PDJB-1274: Update emails to account for other users in the org
            NotificationHelper.sendNotificationAndLogFailures(
                "the swap to individual nudge email for property ownership ${propertyOwnership.id}",
            ) {
                swapToIndividualNudgeEmailService.sendNudgeEmailIfApplicable(propertyOwnership)
            }
        }
    }

    fun addLeftPropertyOwnershipToSession(propertyOwnership: PropertyOwnership) =
        session.setAttribute(
            PROPERTIES_LEFT_THIS_SESSION,
            getLeftPropertyOwnershipsFromSession() + (propertyOwnership.id to propertyOwnership.address.singleLineAddress),
        )

    @Suppress("UNCHECKED_CAST")
    fun getLeftPropertyOwnershipsFromSession(): MutableMap<Long, String> =
        session.getAttribute(PROPERTIES_LEFT_THIS_SESSION) as MutableMap<Long, String>?
            ?: mutableMapOf()
}
