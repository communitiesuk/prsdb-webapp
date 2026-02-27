package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.PropertyComplianceJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig2
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

enum class PropertyComplianceCheckableElements {
    GAS_SAFETY,
    EICR,
    EPC,
    DECLARATION,
}

@JourneyFrameworkComponent
class PropertyComplianceCyaStepConfig :
    AbstractCheckYourAnswersStepConfig2<PropertyComplianceCheckableElements, PropertyComplianceJourneyState>() {
    override fun chooseTemplate(state: PropertyComplianceJourneyState) = "forms/propertyComplianceCheckAnswersForm"

    override fun getStepSpecificContent(state: PropertyComplianceJourneyState): Map<String, Any?> {
        PropertyComplianceCheckableElements.entries.forEach { checkableElement ->
            val newId = state.generateJourneyId("${checkableElement.name} for ${state.journeyId}")
            state.initialiseCyaChildJourney(newId, checkableElement)
        }

        return mapOf(
            "propertyAddress" to "HARDCODED ADDRESS",
            "gasSafetyData" to getGasSafetyData(state),
            "eicrData" to getEicrData(state),
            "epcData" to getEpcData(state),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
        )
    }

    fun getGasSafetyData(state: PropertyComplianceJourneyState) = emptyList<SummaryListRowViewModel>()

    fun getEicrData(state: PropertyComplianceJourneyState) = emptyList<SummaryListRowViewModel>()

    fun getEpcData(state: PropertyComplianceJourneyState) = emptyList<SummaryListRowViewModel>()
}

@JourneyFrameworkComponent
final class PropertyComplianceCyaStep(
    stepConfig: PropertyComplianceCyaStepConfig,
) : AbstractCheckYourAnswersStep<PropertyComplianceCheckableElements, PropertyComplianceJourneyState>(stepConfig)
