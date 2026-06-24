package uk.gov.communities.prsdb.webapp.journeys.noLongerALandlord.stepConfig

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.noLongerALandlord.NoLongerALandlordJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.NoLongerALandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent("noLongerALandlordConfirmStepConfig")
class ConfirmStepConfig(
    private val landlordService: LandlordService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val noLongerALandlordService: NoLongerALandlordService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, NoLongerALandlordJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: NoLongerALandlordJourneyState) =
        mapOf(
            "address" to propertyOwnershipService.getPropertyOwnership(state.propertyOwnershipId).address.singleLineAddress,
            "cancelLinkUrl" to PropertyDetailsController.getPropertyDetailsPath(state.propertyOwnershipId),
        )

    override fun chooseTemplate(state: NoLongerALandlordJourneyState) = "forms/confirmNoLongerALandlordForm"

    override fun mode(state: NoLongerALandlordJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepDataIsAdded(state: NoLongerALandlordJourneyState) {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyOwnershipId)
        val landlord = getLandlordOrThrow(state.baseUserId)

        noLongerALandlordService.leavePropertyOwnership(landlord, propertyOwnership)
        noLongerALandlordService.addLeftPropertyOwnershipToSession(propertyOwnership)
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
        state: NoLongerALandlordJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent("noLongerALandlordConfirmStep")
final class ConfirmStep(
    stepConfig: ConfirmStepConfig,
) : RequestableStep<Complete, NoInputFormModel, NoLongerALandlordJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "confirm"
    }
}
