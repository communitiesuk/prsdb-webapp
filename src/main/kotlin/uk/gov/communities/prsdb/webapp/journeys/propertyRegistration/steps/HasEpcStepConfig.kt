package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO PDJB-656: Implement Has EPC page
@JourneyFrameworkComponent
class HasEpcStepConfig : AbstractRequestableStepConfig<HasEpcMode, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState) = mapOf("todoComment" to "TODO PDJB-656: Implement Has EPC page")

    override fun chooseTemplate(state: JourneyState) = "forms/todo"

    // TODO PDJB-656: Return correct mode based on user choice (has EPC / no EPC / provide later)
    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { HasEpcMode.HAS_EPC }
}

@JourneyFrameworkComponent
final class HasEpcStep(
    stepConfig: HasEpcStepConfig,
) : RequestableStep<HasEpcMode, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "has-epc"
    }
}

enum class HasEpcMode {
    HAS_EPC,
    NO_EPC,
    PROVIDE_LATER,
}
