package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@JourneyFrameworkComponent
class PropertyOccupiedCheckStepConfig : AbstractInternalStepConfig<YesOrNo, EpcState>() {
    override fun mode(state: EpcState): YesOrNo? =
        when (state.isOccupied) {
            true -> YesOrNo.YES
            false -> YesOrNo.NO
            null -> null
        }
}

@JourneyFrameworkComponent
final class PropertyOccupiedCheckStep(
    stepConfig: PropertyOccupiedCheckStepConfig,
) : JourneyStep.InternalStep<YesOrNo, EpcState>(stepConfig)
