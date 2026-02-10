package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState

// TODO PDJB-114: Implement CheckJointLandlordsStep
@JourneyFrameworkComponent
class HasJointLandlordsNavigationConfig : AbstractInternalStepConfig<YesOrNo, JointLandlordsState>() {
    override fun mode(state: JointLandlordsState) = if (state.invitedJointLandlords.isNotEmpty()) YesOrNo.YES else YesOrNo.NO
}

@JourneyFrameworkComponent
final class HasJointLandlordsInternalStep(
    stepConfig: HasJointLandlordsNavigationConfig,
) : JourneyStep.InternalStep<YesOrNo, JointLandlordsState>(stepConfig)
