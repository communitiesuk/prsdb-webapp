package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EicrState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class EicrExemptionConfirmationStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, EicrState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: EicrState): Map<String, Any?> =
        mapOf(
            "title" to "propertyCompliance.title",
            "submitButtonText" to "forms.buttons.saveAndContinueToEPC",
        )

    override fun chooseTemplate(state: EicrState): String = "forms/eicrExemptionConfirmationForm"

    override fun mode(state: EicrState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class EicrExemptionConfirmationStep(
    stepConfig: EicrExemptionConfirmationStepConfig,
) : RequestableStep<Complete, NoInputFormModel, EicrState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "eicr-exemption-confirmation"
    }
}
