package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.propertyManager

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep

enum class PropertyManagerUpdateRouteMode {
    HAS_PROPERTY_MANAGER,
    NO_PROPERTY_MANAGER_ADDED,
}

@JourneyFrameworkComponent
class PropertyManagerUpdateRoutingStepConfig :
    AbstractInternalStepConfig<PropertyManagerUpdateRouteMode, UpdatePropertyManagerJourneyState>() {
    // Uses the stored answers rather than step reachability, as the task's steps are only reachable once this step has an outcome
    override fun mode(state: UpdatePropertyManagerJourneyState): PropertyManagerUpdateRouteMode {
        val propertyManagerTask = state.propertyManagerTask
        val isUsingPropertyManager = propertyManagerTask.propertyManagerStep.formModelOrNull?.usingPropertyManager == true
        val hasPropertyManagerDetails = propertyManagerTask.propertyManagerDetailsStep.formModelOrNull != null
        return if (isUsingPropertyManager && hasPropertyManagerDetails) {
            PropertyManagerUpdateRouteMode.HAS_PROPERTY_MANAGER
        } else {
            PropertyManagerUpdateRouteMode.NO_PROPERTY_MANAGER_ADDED
        }
    }
}

@JourneyFrameworkComponent
class PropertyManagerUpdateRoutingStep(
    stepConfig: PropertyManagerUpdateRoutingStepConfig,
) : InternalStep<PropertyManagerUpdateRouteMode, UpdatePropertyManagerJourneyState>(stepConfig)
