package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyMode
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class HasGasCertStepConfig : AbstractRequestableStepConfig<GasSafetyMode, GasSafetyFormModel, JourneyState>() {
    override val formModelClass = GasSafetyFormModel::class

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
            when (it.hasCert) {
                true -> GasSafetyMode.HAS_CERTIFICATE
                false -> GasSafetyMode.NO_CERTIFICATE
                null -> null
            }
        }
}

@JourneyFrameworkComponent
final class HasGasCertStep(
    stepConfig: HasGasCertStepConfig,
) : RequestableStep<GasSafetyMode, GasSafetyFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "has-gas-safety"
    }
}
