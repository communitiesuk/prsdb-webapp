package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.electricalSafety

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.ElectricalSafetyRegistrationCyaSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.UploadService

@JourneyFrameworkComponent
class UpdateCheckElectricalSafetyAnswersStepConfig(
    private val uploadService: UploadService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, UpdateElectricalSafetyJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: UpdateElectricalSafetyJourneyState): Map<String, Any?> {
        val factory =
            ElectricalSafetyRegistrationCyaSummaryRowsFactory(state, uploadService) { step ->
                Destination.VisitableStep(step, state.getCyaJourneyId(step))
            }
        return mapOf(
            "rows" to factory.createRows(),
            "insetTextKey" to factory.getInsetTextKey(),
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "isTransactionSubmit" to true,
        )
    }

    override fun chooseTemplate(state: UpdateElectricalSafetyJourneyState) = "forms/checkElectricalSafetyAnswersForm"

    override fun mode(state: UpdateElectricalSafetyJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class UpdateCheckElectricalSafetyAnswersStep(
    stepConfig: UpdateCheckElectricalSafetyAnswersStepConfig,
) : RequestableStep<Complete, NoInputFormModel, UpdateElectricalSafetyJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-electrical-safety-answers"
    }
}
