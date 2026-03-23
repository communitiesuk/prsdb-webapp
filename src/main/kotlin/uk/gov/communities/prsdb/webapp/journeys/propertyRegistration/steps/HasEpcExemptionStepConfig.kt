package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO PDJB-657: Implement Has EPC Exemption page
@JourneyFrameworkComponent
class HasEpcExemptionStepConfig : AbstractRequestableStepConfig<HasEpcExemptionMode, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState) = mapOf("todoComment" to "TODO PDJB-657: Implement Has EPC Exemption page")

    override fun chooseTemplate(state: JourneyState) = "forms/todo"

    // TODO PDJB-657: Return correct mode based on user choice (EPC is required / has exemption)
    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { HasEpcExemptionMode.REQUIRED }
}

@JourneyFrameworkComponent
final class HasEpcExemptionStep(
    stepConfig: HasEpcExemptionStepConfig,
) : RequestableStep<HasEpcExemptionMode, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "has-epc-exemption"
    }
}

enum class HasEpcExemptionMode {
    REQUIRED,
    HAS_EXEMPTION,
}
