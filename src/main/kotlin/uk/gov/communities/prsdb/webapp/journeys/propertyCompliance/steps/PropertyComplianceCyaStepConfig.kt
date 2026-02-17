package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.GasSafetyCyaSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.PropertyComplianceJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.UploadService

@JourneyFrameworkComponent
class PropertyComplianceCyaStepConfig(
    private val uploadService: UploadService,
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractCheckYourAnswersStepConfig<PropertyComplianceJourneyState>() {
    override fun chooseTemplate(state: PropertyComplianceJourneyState) = "forms/propertyComplianceCheckAnswersForm"

    override fun getStepSpecificContent(state: PropertyComplianceJourneyState) =
        mapOf(
            "propertyAddress" to propertyOwnershipService.getPropertyOwnership(state.propertyId).address.singleLineAddress,
            "gasSafetyData" to getGasSafetyData(state),
            "eicrData" to getEicrData(state),
            "epcData" to getEpcData(state),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
        )

    fun getGasSafetyData(state: PropertyComplianceJourneyState) =
        GasSafetyCyaSummaryRowsFactory(
            state.gasSafetyStep.outcome == GasSafetyMode.HAS_CERTIFICATE,
            Destination.VisitableStep(state.gasSafetyStep, childJourneyId),
            Destination.VisitableStep(state.gasSafetyExemptionStep, childJourneyId),
            uploadService,
            state,
            childJourneyId,
        ).createRows()

    fun getEicrData(state: PropertyComplianceJourneyState) = emptyList<SummaryListRowViewModel>()

    fun getEpcData(state: PropertyComplianceJourneyState) = emptyList<SummaryListRowViewModel>()
}

@JourneyFrameworkComponent
final class PropertyComplianceCyaStep(
    stepConfig: PropertyComplianceCyaStepConfig,
) : AbstractCheckYourAnswersStep<PropertyComplianceJourneyState>(stepConfig)
