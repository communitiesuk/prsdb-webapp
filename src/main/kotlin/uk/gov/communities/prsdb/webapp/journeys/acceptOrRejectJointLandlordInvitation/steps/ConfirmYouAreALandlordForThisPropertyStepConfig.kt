package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class ConfirmYouAreALandlordForThisPropertyStepConfig(
    private val invitationService: JointLandlordInvitationService,
    private val landlordService: LandlordService,
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

        state.tokenIsValid = invitationService.getTokenIsValid(token)

        if (state.tokenIsValid == true) {
            val invitation = invitationService.getInvitationForJourney(state.journeyId)
            invitationService.storeLastAcceptedPropertyInSession(
                invitation.registeredOwnership.address.toMultiLineAddress(),
                invitation.registeredOwnership.id,
            )
        }

        // TODO PDJB-1056 - Add the current user's landlord record to the property record if the token is still valid
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
