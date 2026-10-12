package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.EmailAddressStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.FullNameStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.JoinOrganisationStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.TokenValidity
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.ValidateTokenStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AcceptOrganisationInvitationEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TokenValidityFormModel

class AcceptOrganisationalLandlordUserInvitationJourneyStateSessionBuilder :
    JourneyStateSessionBuilder<AcceptOrganisationalLandlordUserInvitationJourneyStateSessionBuilder>() {
    companion object {
        fun beforeFullName(): AcceptOrganisationalLandlordUserInvitationJourneyStateSessionBuilder =
            AcceptOrganisationalLandlordUserInvitationJourneyStateSessionBuilder()
                .withSubmittedValue(
                    ValidateTokenStep.ROUTE_SEGMENT,
                    TokenValidityFormModel(TokenValidity.VALID),
                )
                .withSubmittedValue(JoinOrganisationStep.ROUTE_SEGMENT, NoInputFormModel())

        fun beforeEmailAddress(): AcceptOrganisationalLandlordUserInvitationJourneyStateSessionBuilder =
            beforeFullName()
                .withSubmittedValue(
                    FullNameStep.ROUTE_SEGMENT,
                    NameFormModel().apply { name = "Jane Smith" },
                )

        fun beforeCheckAnswers(): AcceptOrganisationalLandlordUserInvitationJourneyStateSessionBuilder =
            beforeEmailAddress()
                .withSubmittedValue(
                    EmailAddressStep.ROUTE_SEGMENT,
                    AcceptOrganisationInvitationEmailFormModel().apply { emailAddress = "invitee@example.com" },
                )
    }
}
