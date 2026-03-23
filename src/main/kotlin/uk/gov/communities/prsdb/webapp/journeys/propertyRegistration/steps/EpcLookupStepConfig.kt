package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep

// TODO (no ticket yet): Implement EPC lookup by UPRN (move logic from EpcQuestionStepConfig.afterStepDataIsAdded())
@JourneyFrameworkComponent
class EpcLookupStepConfig : AbstractInternalStepConfig<EpcLookupMode, JourneyState>() {
    override fun mode(state: JourneyState): EpcLookupMode? = null
}

@JourneyFrameworkComponent
final class EpcLookupStep(
    stepConfig: EpcLookupStepConfig,
) : JourneyStep.InternalStep<EpcLookupMode, JourneyState>(stepConfig)

enum class EpcLookupMode {
    AUTOMATCHED,
    NOT_FOUND,
}
