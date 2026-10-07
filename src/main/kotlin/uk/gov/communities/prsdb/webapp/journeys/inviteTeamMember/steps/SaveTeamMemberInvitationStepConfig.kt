package uk.gov.communities.prsdb.webapp.journeys.inviteTeamMember.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.inviteTeamMember.InviteTeamMemberJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.InviteTeamMemberFormModel
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@JourneyFrameworkComponent
class SaveTeamMemberInvitationStepConfig(
    private val userToLandlordService: UserToLandlordService,
    private val invitationService: OrganisationalLandlordInvitationService,
) : AbstractInternalStepConfig<Complete, InviteTeamMemberJourneyState>() {
    override fun mode(state: InviteTeamMemberJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: InviteTeamMemberJourneyState) {
        val formModel = state.inviteTeamMemberStep.formModel
        invitationService.createInvitation(
            invitedEmail = formModel.notNullValue(InviteTeamMemberFormModel::emailAddress),
            role = formModel.notNullValue(InviteTeamMemberFormModel::role),
            organisationalLandlord = userToLandlordService.getCurrentOrganisationLandlordForUser(),
        )
    }

    override fun resolveNextDestination(
        state: InviteTeamMemberJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class SaveTeamMemberInvitationStep(
    stepConfig: SaveTeamMemberInvitationStepConfig,
) : InternalStep<Complete, InviteTeamMemberJourneyState>(stepConfig)
