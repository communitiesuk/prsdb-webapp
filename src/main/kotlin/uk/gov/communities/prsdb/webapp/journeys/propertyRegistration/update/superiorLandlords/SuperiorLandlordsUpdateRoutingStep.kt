package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.superiorLandlords

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep

enum class SuperiorLandlordsUpdateRouteMode {
    HAS_SUPERIOR_LANDLORDS,
    NO_SUPERIOR_LANDLORDS,
}

@JourneyFrameworkComponent
class SuperiorLandlordsUpdateRoutingStepConfig :
    AbstractInternalStepConfig<SuperiorLandlordsUpdateRouteMode, UpdateSuperiorLandlordsJourneyState>() {
    // Uses the stored answers rather than step reachability, as the task's steps are only reachable once this step has an outcome
    override fun mode(state: UpdateSuperiorLandlordsJourneyState): SuperiorLandlordsUpdateRouteMode {
        val superiorLandlordsTask = state.superiorLandlordsTask
        val hasOwnershipChain = superiorLandlordsTask.anyoneInOwnershipChainStep.formModelOrNull?.anyoneInOwnershipChain == true
        return if (hasOwnershipChain && !superiorLandlordsTask.superiorLandlordsMap.isNullOrEmpty()) {
            SuperiorLandlordsUpdateRouteMode.HAS_SUPERIOR_LANDLORDS
        } else {
            SuperiorLandlordsUpdateRouteMode.NO_SUPERIOR_LANDLORDS
        }
    }
}

@JourneyFrameworkComponent
class SuperiorLandlordsUpdateRoutingStep(
    stepConfig: SuperiorLandlordsUpdateRoutingStepConfig,
) : InternalStep<SuperiorLandlordsUpdateRouteMode, UpdateSuperiorLandlordsJourneyState>(stepConfig)
