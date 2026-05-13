package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.gasSafety

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.GasSafetyRegistrationCyaSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class UpdateCheckGasSafetyAnswersStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, UpdateGasSafetyJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: UpdateGasSafetyJourneyState): Map<String, Any?> {
        val factory =
            GasSafetyRegistrationCyaSummaryRowsFactory(state) { step ->
                Destination.VisitableStep(step, state.getCyaJourneyId(step))
            }
        return mapOf(
            "gasSupplyRows" to factory.createGasSupplyRows(),
            "certRows" to factory.createCertRows(),
            "insetTextKey" to factory.getInsetTextKey(),
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )
    }

    override fun chooseTemplate(state: UpdateGasSafetyJourneyState) = "forms/checkGasSafetyAnswersForm"

    override fun mode(state: UpdateGasSafetyJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class UpdateCheckGasSafetyAnswersStep(
    stepConfig: UpdateCheckGasSafetyAnswersStepConfig,
) : RequestableStep<Complete, NoInputFormModel, UpdateGasSafetyJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-gas-safety-answers"
    }
}
