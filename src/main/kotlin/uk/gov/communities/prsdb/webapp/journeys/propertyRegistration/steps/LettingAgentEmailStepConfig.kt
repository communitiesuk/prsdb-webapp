package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.WhoProvidesDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO PDJB-1390: skeleton stub. Build the real "enter letting agent email address"
// page (input field, validation, persistence).
@JourneyFrameworkComponent
class LettingAgentEmailStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, WhoProvidesDetailsState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: WhoProvidesDetailsState) =
        mapOf<String, Any?>("todoComment" to "TODO PDJB-1390: Enter letting agent email address")

    override fun chooseTemplate(state: WhoProvidesDetailsState): String = "forms/todo"

    override fun mode(state: WhoProvidesDetailsState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class LettingAgentEmailStep(
    stepConfig: LettingAgentEmailStepConfig,
) : RequestableStep<Complete, NoInputFormModel, WhoProvidesDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "letting-agent-email"
    }
}
