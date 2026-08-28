package uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class ConfirmationStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf("todoComment" to "TODO: PDJB-1567: Password creation confirmation page")

    override fun chooseTemplate(state: JourneyState): String = "forms/todo"

    override fun mode(state: JourneyState): Complete = Complete.COMPLETE
}

@JourneyFrameworkComponent
final class ConfirmationStep(
    stepConfig: ConfirmationStepConfig,
) : RequestableStep<Complete, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "confirmation"
    }
}
