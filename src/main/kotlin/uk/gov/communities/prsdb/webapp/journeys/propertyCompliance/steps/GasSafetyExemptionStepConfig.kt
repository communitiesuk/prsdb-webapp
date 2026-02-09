package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class GasSafetyExemptionStepConfig : AbstractRequestableStepConfig<GasSafetyExemptionMode, GasSafetyExemptionFormModel, GasSafetyState>() {
    override val formModelClass = GasSafetyExemptionFormModel::class

    override fun getStepSpecificContent(state: GasSafetyState): Map<String, Any?> =
        mapOf(
            "title" to "propertyCompliance.title",
            "fieldSetHeading" to "forms.gasSafetyExemption.fieldSetHeading",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = true,
                        valueStr = "yes",
                        labelMsgKey = "forms.radios.option.yes.label",
                    ),
                    RadiosButtonViewModel(
                        value = false,
                        valueStr = "no",
                        labelMsgKey = "forms.radios.option.no.label",
                    ),
                ),
        )

    override fun chooseTemplate(state: GasSafetyState): String = "forms/exemptionForm"

    override fun mode(state: GasSafetyState) =
        state.gasSafetyExemptionStep.formModelOrNull?.let {
            when (it.hasExemption) {
                true -> GasSafetyExemptionMode.HAS_EXEMPTION
                false -> GasSafetyExemptionMode.NO_EXEMPTION
                null -> null
            }
        }
}

@JourneyFrameworkComponent
final class GasSafetyExemptionStep(
    stepConfig: GasSafetyExemptionStepConfig,
) : RequestableStep<GasSafetyExemptionMode, GasSafetyExemptionFormModel, GasSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "gas-safety-certificate-exemption"
    }
}

enum class GasSafetyExemptionMode {
    HAS_EXEMPTION,
    NO_EXEMPTION,
}
