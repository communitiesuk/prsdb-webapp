package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.EXEMPTION_OTHER_REASON_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionOtherReasonFormModel

@JourneyFrameworkComponent
class EicrExemptionOtherReasonStepConfig : AbstractRequestableStepConfig<Complete, EicrExemptionOtherReasonFormModel, JourneyState>() {
    override val formModelClass = EicrExemptionOtherReasonFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "forms.eicrExemptionOtherReason.fieldSetHeading",
            "fieldSetHint" to "forms.eicrExemptionOtherReason.fieldSetHint",
            "limit" to EXEMPTION_OTHER_REASON_MAX_LENGTH,
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/exemptionOtherReasonForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.otherReason?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class EicrExemptionOtherReasonStep(
    stepConfig: EicrExemptionOtherReasonStepConfig,
) : RequestableStep<Complete, EicrExemptionOtherReasonFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "eicr-exemption-other-reason"
    }
}
