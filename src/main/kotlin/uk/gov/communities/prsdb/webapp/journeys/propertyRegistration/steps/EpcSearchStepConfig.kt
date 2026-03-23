package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO PDJB-662: Implement EPC Search page
@JourneyFrameworkComponent
class EpcSearchStepConfig : AbstractRequestableStepConfig<EpcSearchMode, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState) = mapOf("todoComment" to "TODO PDJB-662: Implement EPC Search page")

    override fun chooseTemplate(state: JourneyState) = "forms/todo"

    // TODO PDJB-662: Return correct mode based on EPC lookup result (found / superseded / not found)
    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { EpcSearchMode.FOUND }
}

@JourneyFrameworkComponent
final class EpcSearchStep(
    stepConfig: EpcSearchStepConfig,
) : RequestableStep<EpcSearchMode, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "search-for-epc"
    }
}

enum class EpcSearchMode {
    FOUND,
    SUPERSEDED,
    NOT_FOUND,
}
