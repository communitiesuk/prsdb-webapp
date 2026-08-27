package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO(PDJB-1417): this is a skeleton placeholder for the interruption / "are you sure" page. Replace the
//  placeholder todo content with the real interruption card when the page is built.
@JourneyFrameworkComponent
class UpdateOccupancyInterruptionStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, UpdateOccupancyJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: UpdateOccupancyJourneyState) =
        mapOf(
            "todoComment" to "TODO: PDJB-1417 - interruption / are you sure page",
        )

    override fun chooseTemplate(state: UpdateOccupancyJourneyState) = "forms/todo"

    override fun mode(state: UpdateOccupancyJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class UpdateOccupancyInterruptionStep(
    stepConfig: UpdateOccupancyInterruptionStepConfig,
) : RequestableStep<Complete, NoInputFormModel, UpdateOccupancyJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "interruption"
    }
}
