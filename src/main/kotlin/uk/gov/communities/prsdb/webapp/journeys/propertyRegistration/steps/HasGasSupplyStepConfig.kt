package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSupplyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent
class HasGasSupplyStepConfig : AbstractRequestableStepConfig<HasGasSupplyMode, GasSupplyFormModel, GasSafetyDetailState>() {
    override val formModelClass = GasSupplyFormModel::class

    override fun getStepSpecificContent(state: GasSafetyDetailState) =
        mapOf(
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "secondarySubmitButtonText" to "forms.buttons.provideThisLater",
            "submitButtonAction" to CONTINUE_BUTTON_ACTION_NAME,
            "secondarySubmitButtonAction" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME,
            "showSecondarySubmitButton" to state.allowProvideCertificateLaterRoute,
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
        )

    override fun chooseTemplate(state: GasSafetyDetailState) = "forms/gasSupplyForm"

    override fun mode(state: GasSafetyDetailState) =
        getFormModelFromStateOrNull(state)?.let {
            if (it.action == PROVIDE_THIS_LATER_BUTTON_ACTION_NAME) {
                if (state.allowProvideCertificateLaterRoute) {
                    HasGasSupplyMode.PROVIDE_LATER
                } else {
                    throw UnrecoverableJourneyStateException(
                        state.journeyId,
                        "The 'Provide this later' route is not available for this journey",
                    )
                }
            } else {
                when (it.hasGasSupply) {
                    true -> HasGasSupplyMode.HAS_SUPPLY
                    false -> HasGasSupplyMode.NO_SUPPLY
                    null -> null
                }
            }
        }
}

@JourneyFrameworkComponent
final class HasGasSupplyStep(
    stepConfig: HasGasSupplyStepConfig,
) : RequestableStep<HasGasSupplyMode, GasSupplyFormModel, GasSafetyDetailState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "has-gas-supply"
    }
}

enum class HasGasSupplyMode {
    HAS_SUPPLY,
    NO_SUPPLY,
    PROVIDE_LATER,
}
