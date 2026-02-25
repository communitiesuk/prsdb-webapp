package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.EXEMPTION_OTHER_REASON_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionOtherReasonFormModel

@JourneyFrameworkComponent
class GasSafetyExemptionOtherReasonStepConfig :
    AbstractRequestableStepConfig<Complete, GasSafetyExemptionOtherReasonFormModel, JourneyState>() {
    override val formModelClass = GasSafetyExemptionOtherReasonFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "forms.gasSafetyExemptionOtherReason.fieldSetHeading",
            "fieldSetHint" to "forms.gasSafetyExemptionOtherReason.fieldSetHint",
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/exemptionOtherReasonForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.otherReason?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class GasSafetyExemptionOtherReasonStep(
    stepConfig: GasSafetyExemptionOtherReasonStepConfig,
) : RequestableStep<Complete, GasSafetyExemptionOtherReasonFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "gas-safety-certificate-exemption-other-reason"
    }
}
