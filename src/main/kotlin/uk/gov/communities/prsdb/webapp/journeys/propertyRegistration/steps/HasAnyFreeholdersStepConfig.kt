package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers

@JourneyFrameworkComponent
class HasAnyFreeholdersStepConfig : AbstractInternalStepConfig<AnyMembers, FreeholdersDetailsState>() {
    override fun mode(state: FreeholdersDetailsState) =
        if (state.freeholdersMap.isNullOrEmpty()) AnyMembers.NO_MEMBERS else AnyMembers.SOME_MEMBERS
}

@JourneyFrameworkComponent
final class HasAnyFreeholdersStep(
    stepConfig: HasAnyFreeholdersStepConfig,
) : JourneyStep.InternalStep<AnyMembers, FreeholdersDetailsState>(stepConfig)
