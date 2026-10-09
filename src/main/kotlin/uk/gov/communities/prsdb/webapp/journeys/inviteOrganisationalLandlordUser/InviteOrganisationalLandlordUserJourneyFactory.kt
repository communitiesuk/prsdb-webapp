package uk.gov.communities.prsdb.webapp.journeys.inviteOrganisationalLandlordUser

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.inviteOrganisationalLandlordUser.steps.InviteOrganisationalLandlordUserStep
import uk.gov.communities.prsdb.webapp.journeys.inviteOrganisationalLandlordUser.steps.SaveOrganisationalLandlordUserInvitationStep
import uk.gov.communities.prsdb.webapp.journeys.isComplete

@PrsdbWebService
class InviteOrganisationalLandlordUserJourneyFactory(
    private val stateFactory: ObjectFactory<InviteOrganisationalLandlordUserJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepStep { journey.inviteOrganisationalLandlordUserStep }
            step(journey.inviteOrganisationalLandlordUserStep) {
                routeSegment(InviteOrganisationalLandlordUserStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { TEAM_MEMBERS_ROUTE }
                nextStep { journey.saveOrganisationalLandlordUserInvitationStep }
            }
            step(journey.saveOrganisationalLandlordUserInvitationStep) {
                parents { journey.inviteOrganisationalLandlordUserStep.isComplete() }
                // TODO PDJB-1757: Redirect to the invitations tab once it exists
                // TODO PDJB-1762: Show a success banner on the team members page after inviting
                nextUrl { TEAM_MEMBERS_ROUTE }
            }
        }
    }

    fun initializeJourneyState(): String = stateFactory.getObject().initializeState()
}

@JourneyFrameworkComponent
class InviteOrganisationalLandlordUserJourney(
    override val inviteOrganisationalLandlordUserStep: InviteOrganisationalLandlordUserStep,
    override val saveOrganisationalLandlordUserInvitationStep: SaveOrganisationalLandlordUserInvitationStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    InviteOrganisationalLandlordUserJourneyState {
    override fun generateJourneyId(seed: Any?): String =
        super<AbstractJourneyState>.generateJourneyId(
            "Invite organisational landlord user journey at time ${System.currentTimeMillis()}",
        )
}

interface InviteOrganisationalLandlordUserJourneyState : JourneyState {
    val inviteOrganisationalLandlordUserStep: InviteOrganisationalLandlordUserStep
    val saveOrganisationalLandlordUserInvitationStep: SaveOrganisationalLandlordUserInvitationStep
}
