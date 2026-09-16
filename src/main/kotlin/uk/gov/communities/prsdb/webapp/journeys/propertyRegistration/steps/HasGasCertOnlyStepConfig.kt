package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasGasCertFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent
class HasGasCertOnlyStepConfig : AbstractRequestableStepConfig<HasGasCertOnlyMode, HasGasCertFormModel, GasSafetyDetailState>() {
    override val formModelClass = HasGasCertFormModel::class

    override fun getStepSpecificContent(state: GasSafetyDetailState) =
        mapOf(
            "fieldSetHeading" to "propertyCompliance.gasSafetyTask.gasCert.heading",
            "fieldSetHint" to "propertyCompliance.gasSafetyTask.gasCert.hint",
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "submitButtonAction" to CONTINUE_BUTTON_ACTION_NAME,
            "showSecondarySubmitButton" to false,
            "radioOptions" to
                RadiosViewModel.yesOrNoRadios(
                    yesHint = "propertyCompliance.gasSafetyTask.gasCert.radios.yesHint",
                ),
        )

    override fun chooseTemplate(state: GasSafetyDetailState) = "forms/hasGasCertForm"

    override fun mode(state: GasSafetyDetailState) =
        getFormModelFromStateOrNull(state)?.let {
            when (it.hasCert) {
                true -> HasGasCertOnlyMode.YES
                false -> HasGasCertOnlyMode.NO
                null -> null
            }
        }
}

@JourneyFrameworkComponent
final class HasGasCertOnlyStep(
    stepConfig: HasGasCertOnlyStepConfig,
) : RequestableStep<HasGasCertOnlyMode, HasGasCertFormModel, GasSafetyDetailState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "has-gas-safety"
    }
}

enum class HasGasCertOnlyMode {
    YES,
    NO,
}
