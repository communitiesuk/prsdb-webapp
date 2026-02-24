package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class GasSafetyExemptionConfirmationStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, GasSafetyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: GasSafetyState): Map<String, Any?> =
        mapOf(
            "submitButtonText" to if (state.isCheckingAnswers) "forms.buttons.saveAndContinue" else "forms.buttons.saveAndContinueToEICR",
        )

    override fun chooseTemplate(state: GasSafetyState): String = "forms/gasSafetyExemptionConfirmationForm"

    override fun mode(state: GasSafetyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class GasSafetyExemptionConfirmationStep(
    stepConfig: GasSafetyExemptionConfirmationStepConfig,
) : RequestableStep<Complete, NoInputFormModel, GasSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "gas-safety-certificate-exemption-confirmation"
    }
}
