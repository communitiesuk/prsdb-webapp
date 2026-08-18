package uk.gov.communities.prsdb.webapp.journeys.delegateToLettingAgent.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.delegateToLettingAgent.DelegateToLettingAgentJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent("delegateToLettingAgentAllowLettingAgentStepConfig")
class AllowLettingAgentStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, DelegateToLettingAgentJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: DelegateToLettingAgentJourneyState) =
        mapOf(
            "todoComment" to
                "TODO: PDJB-1409 - Allow your letting agent or property manager to provide details page " +
                "(the real page's submit button must use the transactionSubmitButton fragment for metrics)",
        )

    override fun chooseTemplate(state: DelegateToLettingAgentJourneyState) = "forms/todo"

    override fun mode(state: DelegateToLettingAgentJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent("delegateToLettingAgentAllowLettingAgentStep")
final class AllowLettingAgentStep(
    stepConfig: AllowLettingAgentStepConfig,
) : RequestableStep<Complete, NoInputFormModel, DelegateToLettingAgentJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "allow-letting-agent"
    }
}
