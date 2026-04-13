package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.EPC_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.MEES_EXEMPTION_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasEpcFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class HasEpcStepConfig : AbstractRequestableStepConfig<HasEpcMode, HasEpcFormModel, JourneyState>() {
    override val formModelClass = HasEpcFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "propertyCompliance.epcTask.hasEpc.fieldSetHeading",
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "secondarySubmitButtonText" to "propertyCompliance.epcTask.hasEpc.buttons.provideEpcDetailsLater",
            "submitButtonAction" to CONTINUE_BUTTON_ACTION_NAME,
            "secondarySubmitButtonAction" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME,
            "radioOptions" to hasEpcRadios(),
            "meesExemptionGuideUrl" to MEES_EXEMPTION_GUIDE_URL,
            "epcGuideUrl" to EPC_GUIDE_URL,
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

    private fun hasEpcRadios() =
        listOf(
            RadiosButtonViewModel(
                value = true,
                labelMsgKey = "forms.radios.option.yes.label",
            ),
            RadiosButtonViewModel(
                value = false,
                labelMsgKey = "propertyCompliance.epcTask.hasEpc.radios.option.no.label",
            ),
        )
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
