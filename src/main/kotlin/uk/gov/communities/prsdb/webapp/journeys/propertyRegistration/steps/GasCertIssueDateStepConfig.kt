package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.GAS_CERTIFICATE_14_MONTH_EXPIRY
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel

@JourneyFrameworkComponent
class GasCertIssueDateStepConfig(
    private val featureFlagManager: FeatureFlagManager,
) : AbstractRequestableStepConfig<GasCertIssueDateMode, TodayOrPastDateFormModel, GasSafetyDetailState>() {
    override val formModelClass = TodayOrPastDateFormModel::class

    // TODO PDJB-1926: Remove the flag check when GAS_CERTIFICATE_14_MONTH_EXPIRY is removed.
    private val isGasCertificate14MonthExpiryEnabled: Boolean
        get() = featureFlagManager.checkFeature(GAS_CERTIFICATE_14_MONTH_EXPIRY)

    override fun getStepSpecificContent(state: GasSafetyDetailState): Map<String, Any?> {
        val messageKeyPrefix =
            if (isGasCertificate14MonthExpiryEnabled) {
                "propertyCompliance.gasSafetyTask.gasSafetyCertIssueDate"
            } else {
                "propertyCompliance.gasSafetyTask.gasSafetyCertIssueDate.beforePdjb1898"
            }
        return mapOf(
            "fieldSetHeading" to "$messageKeyPrefix.fieldSetHeading",
            "fieldSetHint" to "$messageKeyPrefix.fieldSetHint",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )
    }

    override fun chooseTemplate(state: GasSafetyDetailState): String =
        if (isGasCertificate14MonthExpiryEnabled) "forms/gasCertIssueDateForm" else "forms/dateForm"

    override fun mode(state: GasSafetyDetailState) =
        state.getGasSafetyCertificateIsOutdated()?.let {
            when (it) {
                true -> GasCertIssueDateMode.GAS_SAFETY_CERTIFICATE_OUTDATED
                false -> GasCertIssueDateMode.GAS_SAFETY_CERTIFICATE_IN_DATE
            }
        }
}

@JourneyFrameworkComponent
final class GasCertIssueDateStep(
    stepConfig: GasCertIssueDateStepConfig,
) : RequestableStep<GasCertIssueDateMode, TodayOrPastDateFormModel, GasSafetyDetailState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "gas-safety-certificate-issue-date"
    }
}

enum class GasCertIssueDateMode {
    GAS_SAFETY_CERTIFICATE_OUTDATED,
    GAS_SAFETY_CERTIFICATE_IN_DATE,
}
