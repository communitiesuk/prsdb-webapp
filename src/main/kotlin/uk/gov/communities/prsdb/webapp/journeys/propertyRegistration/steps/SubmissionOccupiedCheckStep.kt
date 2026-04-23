package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.IsOccupiedState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@JourneyFrameworkComponent
class SubmissionOccupiedCheckStepConfig : AbstractInternalStepConfig<YesOrNo, IsOccupiedState>() {
    override fun mode(state: IsOccupiedState): YesOrNo? =
        when (state.isOccupied) {
            true -> YesOrNo.YES
            false -> YesOrNo.NO
            null -> null
        }
}

@JourneyFrameworkComponent
final class SubmissionOccupiedCheckStep(
    stepConfig: SubmissionOccupiedCheckStepConfig,
) : JourneyStep.InternalStep<YesOrNo, IsOccupiedState>(stepConfig)
