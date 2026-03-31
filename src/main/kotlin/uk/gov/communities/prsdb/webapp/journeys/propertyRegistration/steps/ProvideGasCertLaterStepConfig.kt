package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_GAS_SAFETY_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class ProvideGasCertLaterStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, GasSafetyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: GasSafetyState) =
        mapOf(
            "landlordGasSafetyUrl" to LANDLORD_GAS_SAFETY_URL,
            "submitButtonText" to
                if (state.isOccupied == true) "forms.buttons.continue" else "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: GasSafetyState) =
        state.isOccupied?.let { isOccupied ->
            if (isOccupied) {
                "forms/provideGasCertificateLaterForOccupiedProperty"
            } else {
                "forms/provideGasCertificateLaterForUnoccupiedProperty"
            }
        } ?: throw IllegalStateException("ProvideGasCertLaterStep should not be reachable before isOccupied is set")

    override fun mode(state: GasSafetyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class ProvideGasCertLaterStep(
    stepConfig: ProvideGasCertLaterStepConfig,
) : RequestableStep<Complete, NoInputFormModel, GasSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "provide-gas-safety-certificate-later"
    }
}
