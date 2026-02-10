package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EicrState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class EicrExemptionReasonStepConfig : AbstractRequestableStepConfig<Complete, EicrExemptionReasonFormModel, EicrState>() {
    override val formModelClass = EicrExemptionReasonFormModel::class

    override fun getStepSpecificContent(state: EicrState): Map<String, Any?> =
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

    override fun chooseTemplate(state: EicrState): String = "forms/exemptionReasonForm.html"

    override fun mode(state: EicrState) = getFormModelFromStateOrNull(state)?.exemptionReason?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class EicrExemptionReasonStep(
    stepConfig: EicrExemptionReasonStepConfig,
) : RequestableStep<Complete, EicrExemptionReasonFormModel, EicrState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "eicr-exemption-reason"
    }
}
