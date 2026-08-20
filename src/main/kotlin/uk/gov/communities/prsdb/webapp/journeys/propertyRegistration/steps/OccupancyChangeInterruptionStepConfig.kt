package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class OccupancyChangeInterruptionStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, PropertyRegistrationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: PropertyRegistrationJourneyState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: PropertyRegistrationJourneyState) = "forms/occupancyChangeInterruptionForm"

    override fun mode(state: PropertyRegistrationJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepDataIsAdded(state: PropertyRegistrationJourneyState) {
        state.whoProvidesDetailsTask.cachedWhoProvidesRentalDetails = null
        state.clearStepData(WhoProvidesRentalDetailsStep.ROUTE_SEGMENT)
        state.clearStepData(LettingAgentEmailStep.ROUTE_SEGMENT)
        // Return to the task list rather than the CYA page, as the registration is no longer complete. Use
        // StepRoute (route string) not VisitableStep: taskListStep.urlPath is not initialised in this child journey.
        state.returnToCyaPageDestination = Destination.StepRoute(TASK_LIST_PATH_SEGMENT, state.baseJourneyId)
    }
}

@JourneyFrameworkComponent
final class OccupancyChangeInterruptionStep(
    stepConfig: OccupancyChangeInterruptionStepConfig,
) : RequestableStep<Complete, NoInputFormModel, PropertyRegistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "occupancy-change-interruption"
    }
}
