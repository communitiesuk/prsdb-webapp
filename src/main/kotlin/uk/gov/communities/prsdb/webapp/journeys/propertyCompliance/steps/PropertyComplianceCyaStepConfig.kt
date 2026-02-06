package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.PropertyComplianceJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@JourneyFrameworkComponent
class PropertyComplianceCyaStepConfig : AbstractCheckYourAnswersStepConfig<PropertyComplianceJourneyState>() {
    override fun chooseTemplate(state: PropertyComplianceJourneyState) = "forms/propertyComplianceCheckAnswersForm"

    override fun getStepSpecificContent(state: PropertyComplianceJourneyState) =
        mapOf(
            "propertyAddress" to "HARDCODED ADDRESS",
            "gasSafetyData" to getGasSafetyData(state),
            "eicrData" to getEicrData(state),
            "epcData" to getEpcData(state),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
        )

    fun getGasSafetyData(state: PropertyComplianceJourneyState) = emptyList<SummaryListRowViewModel>()

    fun getEicrData(state: PropertyComplianceJourneyState) = emptyList<SummaryListRowViewModel>()

    fun getEpcData(state: PropertyComplianceJourneyState) = emptyList<SummaryListRowViewModel>()
}

@JourneyFrameworkComponent
final class PropertyComplianceCyaStep(
    stepConfig: PropertyComplianceCyaStepConfig,
) : AbstractCheckYourAnswersStep<PropertyComplianceJourneyState>(stepConfig)
