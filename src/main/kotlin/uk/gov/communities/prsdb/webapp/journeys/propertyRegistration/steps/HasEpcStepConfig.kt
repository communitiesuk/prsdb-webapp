package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasEpcFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel.Companion.yesOrNoRadios

@JourneyFrameworkComponent
class HasEpcStepConfig : AbstractRequestableStepConfig<HasEpcMode, HasEpcFormModel, JourneyState>() {
    override val formModelClass = HasEpcFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.epc.fieldSetHeading",
            "fieldSetHint" to "forms.epc.fieldSetHint",
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "secondarySubmitButtonText" to "forms.buttons.provideThisLater",
            "submitButtonAction" to CONTINUE_BUTTON_ACTION_NAME,
            "secondarySubmitButtonAction" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME,
            "radioOptions" to yesOrNoRadios(),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/hasCertForm"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.let {
            if (it.action == PROVIDE_THIS_LATER_BUTTON_ACTION_NAME) {
                HasEpcMode.PROVIDE_LATER
            } else {
                when (it.hasCert) {
                    true -> HasEpcMode.HAS_EPC
                    false -> HasEpcMode.NO_EPC
                    null -> null
                }
            }
        }
}

@JourneyFrameworkComponent
final class HasEpcStep(
    stepConfig: HasEpcStepConfig,
) : RequestableStep<HasEpcMode, HasEpcFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "has-epc"
    }
}

enum class HasEpcMode {
    HAS_EPC,
    NO_EPC,
    PROVIDE_LATER,
}
