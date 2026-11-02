package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.EIC_ISSUE_DATE
import uk.gov.communities.prsdb.webapp.constants.ELECTRICAL_SAFETY_STANDARDS_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class EicExpiredStepConfig(
    private val featureFlagManager: FeatureFlagManager,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, ElectricalSafetyDetailState>() {
    override val formModelClass = NoInputFormModel::class

    // TODO PDJB-1679: Remove the flag check and the expiry date content and templates when the EIC_ISSUE_DATE flag is removed
    private val isEicIssueDateEnabled: Boolean
        get() = featureFlagManager.checkFeature(EIC_ISSUE_DATE)

    override fun getStepSpecificContent(state: ElectricalSafetyDetailState): Map<String, Any?> {
        val dateContent =
            if (isEicIssueDateEnabled) {
                // TODO PDJB-1669: Use the EIC issue date and issue date step instead of expiry date once they exist
                mapOf(
                    "issueDate" to state.getEicExpiryDateIfReachable()?.toJavaLocalDate(),
                    "changeIssueDateUrl" to Destination.VisitableStep(state.eicExpiryDateStep, state.journeyId).toUrlStringOrNull(),
                )
            } else {
                mapOf(
                    "expiryDate" to state.getEicExpiryDateIfReachable()?.toJavaLocalDate(),
                    "changeExpiryDateUrl" to Destination.VisitableStep(state.eicExpiryDateStep, state.journeyId).toUrlStringOrNull(),
                )
            }
        return dateContent +
            mapOf(
                "landlordElectricalSafetyUrl" to ELECTRICAL_SAFETY_STANDARDS_URL,
                "submitButtonText" to
                    if (state.isOccupied) "forms.buttons.continueWithoutElectricalSafety" else "forms.buttons.saveAndContinue",
            )
    }

    override fun chooseTemplate(state: ElectricalSafetyDetailState) =
        when {
            isEicIssueDateEnabled && state.isOccupied -> "forms/eicNeedDifferentCertificateForOccupiedProperty"
            isEicIssueDateEnabled -> "forms/eicNeedDifferentCertificateForUnoccupiedProperty"
            state.isOccupied -> "forms/eicExpiredForOccupiedProperty"
            else -> "forms/eicExpiredForUnoccupiedProperty"
        }

    override fun mode(state: ElectricalSafetyDetailState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class EicExpiredStep(
    stepConfig: EicExpiredStepConfig,
) : RequestableStep<Complete, NoInputFormModel, ElectricalSafetyDetailState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "eic-expired"
    }
}
