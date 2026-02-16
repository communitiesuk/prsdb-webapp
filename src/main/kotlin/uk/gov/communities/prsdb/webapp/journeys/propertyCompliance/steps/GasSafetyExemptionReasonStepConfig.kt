package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class GasSafetyExemptionReasonStepConfig :
    AbstractRequestableStepConfig<GasSafetyExemptionReasonMode, GasSafetyExemptionReasonFormModel, JourneyState>() {
    override val formModelClass = GasSafetyExemptionReasonFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "title" to "propertyCompliance.title",
            "fieldSetHeading" to "forms.gasSafetyExemptionReason.fieldSetHeading",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = GasSafetyExemptionReason.NO_GAS_SUPPLY,
                        labelMsgKey = "forms.gasSafetyExemptionReason.radios.noGas.label",
                    ),
                    RadiosButtonViewModel(
                        value = GasSafetyExemptionReason.LONG_LEASE,
                        labelMsgKey = "forms.gasSafetyExemptionReason.radios.longLease.label",
                        hintMsgKey = "forms.gasSafetyExemptionReason.radios.longLease.hint",
                    ),
                    RadiosButtonViewModel(
                        value = GasSafetyExemptionReason.OTHER,
                        labelMsgKey = "forms.gasSafetyExemptionReason.radios.other.label",
                        hintMsgKey = "forms.gasSafetyExemptionReason.radios.other.hint",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/exemptionReasonForm.html"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.let {
            if (it.exemptionReason == GasSafetyExemptionReason.OTHER) {
                GasSafetyExemptionReasonMode.OTHER_REASON_SELECTED
            } else {
                GasSafetyExemptionReasonMode.LISTED_REASON_SELECTED
            }
        }
}

@JourneyFrameworkComponent
final class GasSafetyExemptionReasonStep(
    stepConfig: GasSafetyExemptionReasonStepConfig,
) : RequestableStep<GasSafetyExemptionReasonMode, GasSafetyExemptionReasonFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "gas-safety-certificate-exemption-reason"
    }
}

enum class GasSafetyExemptionReasonMode {
    LISTED_REASON_SELECTED,
    OTHER_REASON_SELECTED,
}
