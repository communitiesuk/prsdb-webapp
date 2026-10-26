package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.GAS_CERTIFICATE_14_MONTH_EXPIRY
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_GAS_SAFETY_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class GasCertExpiredStepConfig(
    private val featureFlagManager: FeatureFlagManager,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, GasSafetyDetailState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: GasSafetyDetailState) =
        mapOf(
            "issueDate" to
                state.gasCertIssueDateStep.formModelOrNull
                    ?.toLocalDateOrNull(),
            "changeIssueDateUrl" to Destination.VisitableStep(state.gasCertIssueDateStep, state.journeyId).toUrlStringOrNull(),
            "landlordGasSafetyUrl" to LANDLORD_GAS_SAFETY_URL,
            "submitButtonText" to
                if (state.isOccupied == true) "forms.buttons.continueWithoutGasSafety" else "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: GasSafetyDetailState) =
        // TODO PDJB-1926: Remove this flag check and the Before14MonthExpiry templates when the flag is removed.
        if (featureFlagManager.checkFeature(GAS_CERTIFICATE_14_MONTH_EXPIRY)) {
            if (state.isOccupied) {
                "forms/gasSafetyCertificateExpiredForOccupiedProperty"
            } else {
                "forms/gasSafetyCertificateExpiredForUnoccupiedProperty"
            }
        } else {
            if (state.isOccupied) {
                "forms/gasSafetyCertificateExpiredForOccupiedPropertyBefore14MonthExpiry"
            } else {
                "forms/gasSafetyCertificateExpiredForUnoccupiedPropertyBefore14MonthExpiry"
            }
        }

    override fun mode(state: GasSafetyDetailState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class GasCertExpiredStep(
    stepConfig: GasCertExpiredStepConfig,
) : RequestableStep<Complete, NoInputFormModel, GasSafetyDetailState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "gas-safety-certificate-expired"
    }
}
