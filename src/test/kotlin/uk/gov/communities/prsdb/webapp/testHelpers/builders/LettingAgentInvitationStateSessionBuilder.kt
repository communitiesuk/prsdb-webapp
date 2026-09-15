package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps.StartStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

class LettingAgentInvitationStateSessionBuilder : JourneyStateSessionBuilder<LettingAgentInvitationStateSessionBuilder>() {
    fun withStartCompleted(): LettingAgentInvitationStateSessionBuilder {
        withSubmittedValue(StartStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withNoExistingPassword(): LettingAgentInvitationStateSessionBuilder {
        withAdditionalData("hasExistingPassword", "false")
        return self()
    }

    fun withExistingPassword(): LettingAgentInvitationStateSessionBuilder {
        withAdditionalData("hasExistingPassword", "true")
        return self()
    }

    fun withInvitationToken(token: String): LettingAgentInvitationStateSessionBuilder {
        withAdditionalData("invitationToken", "\"$token\"")
        return self()
    }

    companion object {
        fun beforeSetPassword(token: String): LettingAgentInvitationStateSessionBuilder =
            LettingAgentInvitationStateSessionBuilder()
                .withStartCompleted()
                .withNoExistingPassword()
                .withInvitationToken(token)

        fun beforeEnterPassword(token: String): LettingAgentInvitationStateSessionBuilder =
            LettingAgentInvitationStateSessionBuilder()
                .withStartCompleted()
                .withExistingPassword()
                .withInvitationToken(token)
    }
}
