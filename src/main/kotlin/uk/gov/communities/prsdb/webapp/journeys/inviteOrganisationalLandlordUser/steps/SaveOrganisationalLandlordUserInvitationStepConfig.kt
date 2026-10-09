package uk.gov.communities.prsdb.webapp.journeys.inviteOrganisationalLandlordUser.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.inviteOrganisationalLandlordUser.InviteOrganisationalLandlordUserJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.InviteOrganisationalLandlordUserFormModel
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@JourneyFrameworkComponent
class SaveOrganisationalLandlordUserInvitationStepConfig(
    private val userToLandlordService: UserToLandlordService,
    private val invitationService: OrganisationalLandlordInvitationService,
) : AbstractInternalStepConfig<Complete, InviteOrganisationalLandlordUserJourneyState>() {
    override fun mode(state: InviteOrganisationalLandlordUserJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: InviteOrganisationalLandlordUserJourneyState) {
        val formModel = state.inviteOrganisationalLandlordUserStep.formModel
        invitationService.createInvitation(
            invitedEmail = formModel.notNullValue(InviteOrganisationalLandlordUserFormModel::emailAddress),
            role = formModel.notNullValue(InviteOrganisationalLandlordUserFormModel::role),
            organisationalLandlord = userToLandlordService.getCurrentOrganisationLandlordForUser(),
        )
        // TODO PDJB-1761: Send the invitation email to the invited user
    }

    override fun resolveNextDestination(
        state: InviteOrganisationalLandlordUserJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class SaveOrganisationalLandlordUserInvitationStep(
    stepConfig: SaveOrganisationalLandlordUserInvitationStepConfig,
) : InternalStep<Complete, InviteOrganisationalLandlordUserJourneyState>(stepConfig)
