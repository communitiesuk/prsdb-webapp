package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationAcceptedEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationAcceptedOtherLandlordEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class ConfirmYouAreALandlordForThisPropertyStepConfig(
    private val invitationService: JointLandlordInvitationService,
    private val landlordService: LandlordService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val acceptedEmailSender: EmailNotificationService<JointLandlordInvitationAcceptedEmail>,
    private val otherLandlordEmailSender: EmailNotificationService<JointLandlordInvitationAcceptedOtherLandlordEmail>,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, AcceptOrRejectJointLandlordInvitationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: AcceptOrRejectJointLandlordInvitationJourneyState): Map<String, Any?> {
        val invitation = invitationService.getInvitationForJourney(state.journeyId)

        val propertyAddress = invitation.registeredOwnership.address.toMultiLineAddress().split("\n")

        val registrationNumber =
            if (state.userCompletedLandlordRegistrationThisJourney == true) {
                val baseUserId = SecurityContextHolder.getContext().authentication.name
                val landlord =
                    landlordService.retrieveLandlordByBaseUserId(baseUserId)
                        ?: throw PrsdbWebException(
                            "Landlord record not found for user with baseUserId $baseUserId after they completed landlord registration",
                        )
                RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber).toString()
            } else {
                null
            }

        return mapOf(
            "heading" to "acceptOrRejectJointLandlordInvitation.confirmLandlordForProperty.heading",
            "propertyAddress" to propertyAddress,
            "showSuccessBanner" to (registrationNumber != null),
            "registrationNumber" to registrationNumber,
        )
    }

    override fun chooseTemplate(state: AcceptOrRejectJointLandlordInvitationJourneyState) =
        "forms/confirmYouAreALandlordForThisPropertyForm"

    override fun mode(state: AcceptOrRejectJointLandlordInvitationJourneyState): Complete? =
        getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepDataIsAdded(state: AcceptOrRejectJointLandlordInvitationJourneyState) {
        val token = invitationService.getInvitationTokenForJourneyIdFromSession(state.journeyId)

        val tokenIsValid = invitationService.getTokenIsValid(token)
        state.tokenIsValid = tokenIsValid

        if (tokenIsValid) {
            val invitation = invitationService.getInvitationForJourney(state.journeyId)
            val propertyOwnership = invitation.registeredOwnership
            invitationService.storeLastAcceptedPropertyInSession(
                propertyOwnership.address.toMultiLineAddress(),
                propertyOwnership.id,
            )
            sendAcceptanceEmails(propertyOwnership)
        }

        // TODO PDJB-1056 - Add the current user's landlord record to the property record if the token is still valid
    }

    private fun sendAcceptanceEmails(propertyOwnership: PropertyOwnership) {
        val baseUserId = SecurityContextHolder.getContext().authentication.name
        val acceptingLandlord = landlordService.retrieveLandlordByBaseUserId(baseUserId) ?: return

        val propertyAddress = propertyOwnership.address.toMultiLineAddress()
        val propertyRecordUrl = absoluteUrlProvider.buildPropertyDetailsUri(propertyOwnership.id).toString()
        val propertyRegistrationNumber =
            RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString()

        acceptedEmailSender.sendEmail(
            acceptingLandlord.email,
            JointLandlordInvitationAcceptedEmail(
                recipientName = acceptingLandlord.name,
                propertyAddress = propertyAddress,
                propertyRecordUrl = propertyRecordUrl,
                propertyRegistrationNumber = propertyRegistrationNumber,
            ),
        )

        propertyOwnership.landlords
            .filter { it.id != acceptingLandlord.id }
            .forEach { landlord ->
                otherLandlordEmailSender.sendEmail(
                    landlord.email,
                    JointLandlordInvitationAcceptedOtherLandlordEmail(
                        recipientName = landlord.name,
                        acceptedLandlordName = acceptingLandlord.name,
                        propertyAddress = propertyAddress,
                        propertyRecordUrl = propertyRecordUrl,
                    ),
                )
            }
    }
}

@JourneyFrameworkComponent
final class ConfirmYouAreALandlordForThisPropertyStep(
    stepConfig: ConfirmYouAreALandlordForThisPropertyStepConfig,
) : RequestableStep<Complete, NoInputFormModel, AcceptOrRejectJointLandlordInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "confirm-landlord-for-property"
    }
}
