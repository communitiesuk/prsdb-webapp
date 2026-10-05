package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps.JoinOrganisationStep
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps.TokenValidity
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps.ValidateTokenStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TokenValidityFormModel

class AcceptInvitationJourneyStateSessionBuilder : JourneyStateSessionBuilder<AcceptInvitationJourneyStateSessionBuilder>() {
    companion object {
        fun beforeFullName(): AcceptInvitationJourneyStateSessionBuilder =
            AcceptInvitationJourneyStateSessionBuilder()
                .withSubmittedValue(
                    ValidateTokenStep.ROUTE_SEGMENT,
                    TokenValidityFormModel(TokenValidity.VALID),
                )
                .withSubmittedValue(JoinOrganisationStep.ROUTE_SEGMENT, NoInputFormModel())
    }
}
