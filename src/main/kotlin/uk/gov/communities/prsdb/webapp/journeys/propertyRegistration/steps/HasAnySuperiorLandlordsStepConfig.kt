package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers

@JourneyFrameworkComponent
class HasAnySuperiorLandlordsStepConfig : AbstractInternalStepConfig<AnyMembers, SuperiorLandlordsDetailsState>() {
    override fun mode(state: SuperiorLandlordsDetailsState) =
        if (state.superiorLandlordsMap.isNullOrEmpty()) AnyMembers.NO_MEMBERS else AnyMembers.SOME_MEMBERS
}

@JourneyFrameworkComponent
final class HasAnySuperiorLandlordsStep(
    stepConfig: HasAnySuperiorLandlordsStepConfig,
) : JourneyStep.InternalStep<AnyMembers, SuperiorLandlordsDetailsState>(stepConfig)
