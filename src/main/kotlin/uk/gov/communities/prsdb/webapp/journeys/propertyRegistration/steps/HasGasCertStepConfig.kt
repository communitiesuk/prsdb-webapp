package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasGasCertFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class HasGasCertStepConfig : AbstractRequestableStepConfig<HasGasCertMode, HasGasCertFormModel, JourneyState>() {
    override val formModelClass = HasGasCertFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = true,
                        valueStr = "yes",
                        labelMsgKey = "forms.radios.option.yes.label",
                        hintMsgKey = "propertyCompliance.gasSafetyTask.gasCert.radios.yesHint",
                    ),
                    RadiosButtonViewModel(
                        value = false,
                        valueStr = "no",
                        labelMsgKey = "forms.radios.option.no.label",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/hasCertForm"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.let {
            if (it.action == "provideThisLater") {
                HasGasCertMode.PROVIDE_THIS_LATER
            } else {
                when (it.hasCert) {
                    true -> HasGasCertMode.HAS_CERTIFICATE
                    false -> HasGasCertMode.NO_CERTIFICATE
                    null -> null
                }
            }
        }
}

@JourneyFrameworkComponent
final class HasGasCertStep(
    stepConfig: HasGasCertStepConfig,
) : RequestableStep<HasGasCertMode, HasGasCertFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "has-gas-safety"
    }
}

enum class HasGasCertMode {
    HAS_CERTIFICATE,
    NO_CERTIFICATE,
    PROVIDE_THIS_LATER,
}
