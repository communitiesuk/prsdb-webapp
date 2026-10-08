package uk.gov.communities.prsdb.webapp.journeys.inviteTeamMember

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.inviteTeamMember.steps.InviteTeamMemberStep
import uk.gov.communities.prsdb.webapp.journeys.inviteTeamMember.steps.SaveTeamMemberInvitationStep
import uk.gov.communities.prsdb.webapp.journeys.isComplete

@PrsdbWebService
class InviteTeamMemberJourneyFactory(
    private val stateFactory: ObjectFactory<InviteTeamMemberJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepStep { journey.inviteTeamMemberStep }
            step(journey.inviteTeamMemberStep) {
                routeSegment(InviteTeamMemberStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { TEAM_MEMBERS_ROUTE }
                nextStep { journey.saveTeamMemberInvitationStep }
            }
            step(journey.saveTeamMemberInvitationStep) {
                parents { journey.inviteTeamMemberStep.isComplete() }
                // TODO PDJB-1757: Redirect to the invitations tab once it exists
                // TODO PDJB-1762: Show a success banner on the team members page after inviting
                nextUrl { TEAM_MEMBERS_ROUTE }
            }
        }
    }

    fun initializeJourneyState(): String = stateFactory.getObject().initializeState()
}

@JourneyFrameworkComponent
class InviteTeamMemberJourney(
    override val inviteTeamMemberStep: InviteTeamMemberStep,
    override val saveTeamMemberInvitationStep: SaveTeamMemberInvitationStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    InviteTeamMemberJourneyState {
    override fun generateJourneyId(seed: Any?): String =
        super<AbstractJourneyState>.generateJourneyId(
            "Invite team member journey at time ${System.currentTimeMillis()}",
        )
}

interface InviteTeamMemberJourneyState : JourneyState {
    val inviteTeamMemberStep: InviteTeamMemberStep
    val saveTeamMemberInvitationStep: SaveTeamMemberInvitationStep
}
