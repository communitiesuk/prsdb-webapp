package uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.states.InviteJointLandlordState

@JourneyFrameworkComponent
class IsMarkedAsJointLandlordStepConfig : AbstractInternalStepConfig<YesOrNo, InviteJointLandlordState>() {
    override fun mode(state: InviteJointLandlordState): YesOrNo = if (state.propertyMarkedAsJointLandlord) YesOrNo.YES else YesOrNo.NO
}

@JourneyFrameworkComponent
final class IsMarkedAsJointLandlordStep(
    stepConfig: IsMarkedAsJointLandlordStepConfig,
) : JourneyStep.InternalStep<YesOrNo, InviteJointLandlordState>(stepConfig)
