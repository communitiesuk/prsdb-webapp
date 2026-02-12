package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class EpcExemptionConfirmationStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, EpcState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: EpcState) =
        mapOf(
            "title" to "propertyCompliance.title",
            "submitButtonText" to "forms.buttons.saveAndContinueToLandlordResponsibilities",
        )

    override fun chooseTemplate(state: EpcState): String = "forms/epcExemptionConfirmationForm"

    override fun mode(state: EpcState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class EpcExemptionConfirmationStep(
    stepConfig: EpcExemptionConfirmationStepConfig,
) : RequestableStep<Complete, NoInputFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "epc-exemption-confirmation"
    }
}
