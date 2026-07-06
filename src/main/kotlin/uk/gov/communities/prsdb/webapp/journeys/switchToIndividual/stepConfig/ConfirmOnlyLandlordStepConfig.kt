package uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.SwitchToIndividualJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class ConfirmOnlyLandlordStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, SwitchToIndividualJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: SwitchToIndividualJourneyState): Map<String, Any?> =
        mapOf(
            "addressParts" to
                propertyOwnershipService
                    .getPropertyOwnership(state.propertyOwnershipId)
                    .address
                    .toMultiLineAddress()
                    .split("\n"),
            "cancelLinkUrl" to PropertyDetailsController.getPropertyDetailsPath(state.propertyOwnershipId),
        )

    override fun chooseTemplate(state: SwitchToIndividualJourneyState) = "forms/confirmOnlyLandlordForm"

    override fun mode(state: SwitchToIndividualJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class ConfirmOnlyLandlordStep(
    stepConfig: ConfirmOnlyLandlordStepConfig,
) : RequestableStep<Complete, NoInputFormModel, SwitchToIndividualJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "confirm"
    }
}
