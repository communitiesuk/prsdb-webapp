package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_GAS_SAFETY_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class GasCertMissingStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, GasSafetyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: GasSafetyState) =
        mapOf(
            "landlordGasSafetyUrl" to LANDLORD_GAS_SAFETY_URL,
            "submitButtonText" to if (state.isOccupied == true) "forms.buttons.continueWithoutGasSafety" else "forms.buttons.continue",
        )

    override fun chooseTemplate(state: GasSafetyState) =
        state.isOccupied?.let { isOccupied ->
            if (isOccupied) {
                "forms/gasSafetyCertificateMissingForOccupiedProperty"
            } else {
                "forms/gasSafetyCertificateMissingForUnoccupiedProperty"
            }
        } ?: throw IllegalStateException("GasCertMissingStep should not reachable before isOccupied is set")

    override fun mode(state: GasSafetyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class GasCertMissingStep(
    stepConfig: GasCertMissingStepConfig,
) : RequestableStep<Complete, NoInputFormModel, GasSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "gas-safety-certificate-missing"
    }
}
