package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EicrState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class EicrExemptionStepConfig : AbstractRequestableStepConfig<EicrExemptionMode, EicrExemptionFormModel, EicrState>() {
    override val formModelClass = EicrExemptionFormModel::class

    override fun getStepSpecificContent(state: EicrState): Map<String, Any?> =
        mapOf(
            "title" to "propertyCompliance.title",
            "fieldSetHeading" to "forms.eicrExemption.fieldSetHeading",
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

    override fun chooseTemplate(state: EicrState): String = "forms/exemptionForm"

    override fun mode(state: EicrState) =
        state.eicrExemptionStep.formModelOrNull?.let {
            when (it.hasExemption) {
                true -> EicrExemptionMode.HAS_EXEMPTION
                false -> EicrExemptionMode.NO_EXEMPTION
                null -> null
            }
        }
}

@JourneyFrameworkComponent
final class EicrExemptionStep(
    stepConfig: EicrExemptionStepConfig,
) : RequestableStep<EicrExemptionMode, EicrExemptionFormModel, EicrState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "eicr-exemption"
    }
}

enum class EicrExemptionMode {
    HAS_EXEMPTION,
    NO_EXEMPTION,
}
