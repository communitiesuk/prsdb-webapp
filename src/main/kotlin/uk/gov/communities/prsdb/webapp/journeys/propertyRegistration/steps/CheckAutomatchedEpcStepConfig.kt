package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO PDJB-661: Implement Check Automatched EPC page
@JourneyFrameworkComponent
class CheckAutomatchedEpcStepConfig : AbstractRequestableStepConfig<CheckEpcMode, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState) = mapOf("todoComment" to "TODO PDJB-661: Implement Check Automatched EPC page")

    override fun chooseTemplate(state: JourneyState) = "forms/todo"

    // TODO PDJB-661: Return correct mode based on user choice and EPC data (age, energy rating, occupancy)
    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { CheckEpcMode.UNOCCUPIED }
}

@JourneyFrameworkComponent
final class CheckAutomatchedEpcStep(
    stepConfig: CheckAutomatchedEpcStepConfig,
) : RequestableStep<CheckEpcMode, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-automatched-epc"
    }
}

enum class CheckEpcMode {
    SEARCH_AGAIN,
    OLD_LOW_RATING,
    OCCUPIED,
    UNOCCUPIED,
}
