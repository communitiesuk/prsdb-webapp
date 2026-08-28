package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps.StartStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

class LettingAgentInvitationStateSessionBuilder : JourneyStateSessionBuilder<LettingAgentInvitationStateSessionBuilder>() {
    fun withStartCompleted(): LettingAgentInvitationStateSessionBuilder {
        withSubmittedValue(StartStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }
}
