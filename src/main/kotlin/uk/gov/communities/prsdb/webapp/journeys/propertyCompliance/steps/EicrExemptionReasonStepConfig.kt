package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class EicrExemptionReasonStepConfig : AbstractRequestableStepConfig<EicrExemptionReasonMode, EicrExemptionReasonFormModel, JourneyState>() {
    override val formModelClass = EicrExemptionReasonFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "title" to "propertyCompliance.title",
            "fieldSetHeading" to "forms.eicrExemptionReason.fieldSetHeading",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = EicrExemptionReason.LONG_LEASE,
                        labelMsgKey = "forms.eicrExemptionReason.radios.longLease.label",
                        hintMsgKey = "forms.eicrExemptionReason.radios.longLease.hint",
                    ),
                    RadiosButtonViewModel(
                        value = EicrExemptionReason.OTHER,
                        labelMsgKey = "forms.eicrExemptionReason.radios.other.label",
                        hintMsgKey = "forms.eicrExemptionReason.radios.other.hint",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/exemptionReasonForm"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.let {
            if (it.exemptionReason == EicrExemptionReason.OTHER) {
                EicrExemptionReasonMode.OTHER_REASON_SELECTED
            } else {
                EicrExemptionReasonMode.LISTED_REASON_SELECTED
            }
        }
}

@JourneyFrameworkComponent
final class EicrExemptionReasonStep(
    stepConfig: EicrExemptionReasonStepConfig,
) : RequestableStep<EicrExemptionReasonMode, EicrExemptionReasonFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "eicr-exemption-reason"
    }
}

enum class EicrExemptionReasonMode {
    LISTED_REASON_SELECTED,
    OTHER_REASON_SELECTED,
}
