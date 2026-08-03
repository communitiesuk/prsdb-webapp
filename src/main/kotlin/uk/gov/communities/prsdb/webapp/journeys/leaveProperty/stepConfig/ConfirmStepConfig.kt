package uk.gov.communities.prsdb.webapp.journeys.leaveProperty.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.leaveProperty.LeavePropertyJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.LeavePropertyService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@JourneyFrameworkComponent("leavePropertyConfirmStepConfig")
class ConfirmStepConfig(
    private val userToLandlordService: UserToLandlordService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val leavePropertyService: LeavePropertyService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, LeavePropertyJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: LeavePropertyJourneyState) =
        mapOf(
            "address" to propertyOwnershipService.getPropertyOwnership(state.propertyOwnershipId).address.singleLineAddress,
            "cancelLinkUrl" to PropertyDetailsController.getPropertyDetailsPath(state.propertyOwnershipId),
        )

    override fun chooseTemplate(state: LeavePropertyJourneyState) = "forms/confirmLeavePropertyForm"

    override fun mode(state: LeavePropertyJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepDataIsAdded(state: LeavePropertyJourneyState) {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyOwnershipId)
        val landlord = userToLandlordService.getCurrentLandlordForUser()

        leavePropertyService.leavePropertyOwnership(landlord, propertyOwnership)
        leavePropertyService.addLeftPropertyOwnershipToSession(propertyOwnership)
    }

    override fun resolveNextDestination(
        state: LeavePropertyJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent("leavePropertyConfirmStep")
final class ConfirmStep(
    stepConfig: ConfirmStepConfig,
) : RequestableStep<Complete, NoInputFormModel, LeavePropertyJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "confirm"
    }
}
