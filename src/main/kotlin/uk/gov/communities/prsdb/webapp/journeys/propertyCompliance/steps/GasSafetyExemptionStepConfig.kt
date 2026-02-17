package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.ExemptionMode
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent
class GasSafetyExemptionStepConfig : AbstractRequestableStepConfig<ExemptionMode, GasSafetyExemptionFormModel, JourneyState>() {
    override val formModelClass = GasSafetyExemptionFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "title" to "propertyCompliance.title",
            "fieldSetHeading" to "forms.gasSafetyExemption.fieldSetHeading",
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/exemptionForm"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.let {
            when (it.hasExemption) {
                true -> ExemptionMode.HAS_EXEMPTION
                false -> ExemptionMode.NO_EXEMPTION
                null -> null
            }
        }
}

@JourneyFrameworkComponent
final class GasSafetyExemptionStep(
    stepConfig: GasSafetyExemptionStepConfig,
) : RequestableStep<ExemptionMode, GasSafetyExemptionFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "gas-safety-certificate-exemption"
    }
}
