package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFE_REGISTER_URL
import uk.gov.communities.prsdb.webapp.constants.HSE_URL
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_GAS_SAFETY_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class GasSafetyExemptionMissingStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, GasSafetyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: GasSafetyState): Map<String, Any?> =
        mapOf(
            "title" to "propertyCompliance.title",
            "gasSafeRegisterUrl" to GAS_SAFE_REGISTER_URL,
            "hseUrl" to HSE_URL,
            "landlordGasSafetyUrl" to LANDLORD_GAS_SAFETY_URL,
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: GasSafetyState): String = "forms/gasSafetyExemptionMissingForm"

    override fun mode(state: GasSafetyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class GasSafetyExemptionMissingStep(
    stepConfig: GasSafetyExemptionMissingStepConfig,
) : RequestableStep<Complete, NoInputFormModel, GasSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "gas-safety-certificate-exemption-missing"
    }
}
