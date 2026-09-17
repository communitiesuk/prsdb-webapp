package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasGasCertFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent
class BeforePdjb1022HasGasCertStepConfig :
    AbstractRequestableStepConfig<BeforePdjb1022HasGasCertMode, HasGasCertFormModel, GasSafetyDetailState>() {
    override val formModelClass = HasGasCertFormModel::class

    override fun getStepSpecificContent(state: GasSafetyDetailState) =
        mapOf(
            "fieldSetHeading" to "propertyCompliance.gasSafetyTask.gasCert.heading",
            "fieldSetHint" to "propertyCompliance.gasSafetyTask.gasCert.hint",
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "secondarySubmitButtonText" to "forms.buttons.provideThisLater",
            "submitButtonAction" to CONTINUE_BUTTON_ACTION_NAME,
            "secondarySubmitButtonAction" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME,
            "showSecondarySubmitButton" to state.allowProvideCertificateLaterRoute,
            "radioOptions" to
                RadiosViewModel.yesOrNoRadios(
                    yesHint = "propertyCompliance.gasSafetyTask.gasCert.radios.yesHint",
                ),
        )

    override fun chooseTemplate(state: GasSafetyDetailState) = "forms/hasGasCertForm"

    override fun mode(state: GasSafetyDetailState) =
        getFormModelFromStateOrNull(state)?.let {
            if (it.action == PROVIDE_THIS_LATER_BUTTON_ACTION_NAME) {
                if (state.allowProvideCertificateLaterRoute) {
                    BeforePdjb1022HasGasCertMode.PROVIDE_THIS_LATER
                } else {
                    throw UnrecoverableJourneyStateException(
                        state.journeyId,
                        "The 'Provide this later' route is not available for this journey",
                    )
                }
            } else {
                when (it.hasCert) {
                    true -> BeforePdjb1022HasGasCertMode.HAS_CERTIFICATE
                    false -> BeforePdjb1022HasGasCertMode.NO_CERTIFICATE
                    null -> null
                }
            }
        }
}

@JourneyFrameworkComponent
final class BeforePdjb1022HasGasCertStep(
    stepConfig: BeforePdjb1022HasGasCertStepConfig,
) : RequestableStep<BeforePdjb1022HasGasCertMode, HasGasCertFormModel, GasSafetyDetailState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "has-gas-safety"
    }
}

enum class BeforePdjb1022HasGasCertMode {
    HAS_CERTIFICATE,
    NO_CERTIFICATE,
    PROVIDE_THIS_LATER,
}
