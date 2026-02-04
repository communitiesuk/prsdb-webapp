package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.PropertyComplianceJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig

@JourneyFrameworkComponent
class PropertyComplianceCyaStepConfig : AbstractCheckYourAnswersStepConfig<PropertyComplianceJourneyState>() {
    override fun getStepSpecificContent(state: PropertyComplianceJourneyState): Map<String, Any?> {
        TODO("Not yet implemented")
    }
}

@JourneyFrameworkComponent
final class PropertyComplianceCyaStep(
    stepConfig: PropertyComplianceCyaStepConfig,
) : AbstractCheckYourAnswersStep<PropertyComplianceJourneyState>(stepConfig)
