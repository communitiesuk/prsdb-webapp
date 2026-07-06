package uk.gov.communities.prsdb.webapp.journeys.leaveProperty.stepConfig

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.leaveProperty.LeavePropertyJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.LeavePropertyService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent("leavePropertyConfirmStepConfig")
class ConfirmStepConfig(
    private val landlordService: LandlordService,
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
        val landlord = getLandlordOrThrow(state.baseUserId)

        leavePropertyService.leavePropertyOwnership(landlord, propertyOwnership)
        leavePropertyService.addLeftPropertyOwnershipToSession(propertyOwnership)
    }

    private fun getLandlordOrThrow(baseUserId: String): Landlord =
        (
            landlordService.retrieveLandlordByBaseUserId(baseUserId)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Landlord not found for user $baseUserId",
                )
        )

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
