package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_GAS_SAFETY_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class GasCertExpiredStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, GasSafetyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: GasSafetyState) =
        mapOf(
            "issueDate" to
                state.gasCertIssueDateStep.formModelOrNull
                    ?.toLocalDateOrNull(),
            // TODO PDJB-637 - check if this works for CYA, probably need to pass the cya child journey id here.
            "changeIssueDateUrl" to Destination.VisitableStep(state.gasCertIssueDateStep, state.journeyId).toUrlStringOrNull(),
            "landlordGasSafetyUrl" to LANDLORD_GAS_SAFETY_URL,
            "submitButtonText" to
                if (state.isOccupied == true) "forms.buttons.continueWithoutGasSafety" else "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: GasSafetyState) =
        state.isOccupied?.let { isOccupied ->
            if (isOccupied) {
                "forms/gasSafetyCertificateExpiredForOccupiedProperty"
            } else {
                "forms/gasSafetyCertificateExpiredForUnoccupiedProperty"
            }
        } ?: throw IllegalStateException("GasCertExpiredStep should not reachable before isOccupied is set")

    override fun mode(state: GasSafetyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class GasCertExpiredStep(
    stepConfig: GasCertExpiredStepConfig,
) : RequestableStep<Complete, NoInputFormModel, GasSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "gas-safety-certificate-expired"
    }
}
