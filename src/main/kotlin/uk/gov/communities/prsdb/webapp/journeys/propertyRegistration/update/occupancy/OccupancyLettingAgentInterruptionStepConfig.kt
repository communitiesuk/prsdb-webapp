package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class OccupancyLettingAgentInterruptionStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, UpdateOccupancyJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: UpdateOccupancyJourneyState): Map<String, Any> = emptyMap()

    override fun chooseTemplate(state: UpdateOccupancyJourneyState): String = "forms/occupancyLettingAgentInterruptionForm"

    override fun mode(state: UpdateOccupancyJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OccupancyLettingAgentInterruptionStep(
    stepConfig: OccupancyLettingAgentInterruptionStepConfig,
) : RequestableStep<Complete, NoInputFormModel, UpdateOccupancyJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "are-you-sure"
    }
}
