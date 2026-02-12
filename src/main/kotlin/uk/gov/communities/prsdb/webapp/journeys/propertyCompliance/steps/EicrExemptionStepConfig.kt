package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class EicrExemptionStepConfig : AbstractRequestableStepConfig<EicrExemptionMode, EicrExemptionFormModel, JourneyState>() {
    override val formModelClass = EicrExemptionFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
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

    override fun chooseTemplate(state: JourneyState): String = "forms/exemptionForm"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.let {
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
) : RequestableStep<EicrExemptionMode, EicrExemptionFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "eicr-exemption"
    }
}

enum class EicrExemptionMode {
    HAS_EXEMPTION,
    NO_EXEMPTION,
}
