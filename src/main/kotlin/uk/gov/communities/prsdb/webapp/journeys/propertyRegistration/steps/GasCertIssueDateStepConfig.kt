package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel

@JourneyFrameworkComponent
class GasCertIssueDateStepConfig : AbstractRequestableStepConfig<GasCertIssueDateMode, TodayOrPastDateFormModel, GasSafetyState>() {
    override val formModelClass = TodayOrPastDateFormModel::class

    override fun getStepSpecificContent(state: GasSafetyState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "forms.todayOrPastDate.gasSafetyCert.fieldSetHeading",
            "fieldSetHint" to "forms.todayOrPastDate.gasSafetyCert.fieldSetHint",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: GasSafetyState): String = "forms/dateForm"

    override fun mode(state: GasSafetyState) =
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
) : RequestableStep<GasCertIssueDateMode, TodayOrPastDateFormModel, GasSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "gas-safety-certificate-issue-date"
    }
}

enum class GasCertIssueDateMode {
    GAS_SAFETY_CERTIFICATE_OUTDATED,
    GAS_SAFETY_CERTIFICATE_IN_DATE,
}
