package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSupplyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent
class BeforePdjb1022HasGasSupplyStepConfig : AbstractRequestableStepConfig<YesOrNo, GasSupplyFormModel, JourneyState>() {
    override val formModelClass = GasSupplyFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "submitButtonAction" to CONTINUE_BUTTON_ACTION_NAME,
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/gasSupplyForm"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.let {
            when (it.hasGasSupply) {
                true -> YesOrNo.YES
                false -> YesOrNo.NO
                null -> null
            }
        }
}

@JourneyFrameworkComponent
final class BeforePdjb1022HasGasSupplyStep(
    stepConfig: BeforePdjb1022HasGasSupplyStepConfig,
) : RequestableStep<YesOrNo, GasSupplyFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "has-gas-supply"
    }
}
