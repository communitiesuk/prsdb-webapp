package uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.LettingAgentInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider

@JourneyFrameworkComponent
class ConfirmationStepConfig(
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, LettingAgentInvitationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: LettingAgentInvitationJourneyState): Map<String, Any?> {
        val token = requireNotNull(state.invitationToken) { "Invitation token is missing from the journey state" }
        // The letting agent returns to their property using the same invitation link that was emailed to them.
        val updateLink = absoluteUrlProvider.buildLettingAgentInvitationUri(token).toString()
        return mapOf(
            "summaryListRows" to
                listOf(
                    SummaryListRowViewModel(
                        fieldHeading = "lettingAgentInvitation.confirmation.updateLink.key",
                        fieldValue = updateLink,
                        valueUrl = updateLink,
                    ),
                    SummaryListRowViewModel(
                        fieldHeading = "lettingAgentInvitation.confirmation.password.key",
                        fieldValue = "lettingAgentInvitation.confirmation.password.value",
                    ),
                ),
        )
    }

    override fun chooseTemplate(state: LettingAgentInvitationJourneyState): String = "forms/lettingAgentInvitationConfirmation"

    override fun mode(state: LettingAgentInvitationJourneyState): Complete = Complete.COMPLETE
}

@JourneyFrameworkComponent
final class ConfirmationStep(
    stepConfig: ConfirmationStepConfig,
) : RequestableStep<Complete, NoInputFormModel, LettingAgentInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "confirmation"
    }
}
