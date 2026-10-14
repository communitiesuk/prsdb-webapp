package uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.ChangeOrgTeamMemberAccessLevelJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService

@JourneyFrameworkComponent
class CompleteChangeAccessLevelStepConfig(
    private val organisationalLandlordUserService: OrganisationalLandlordUserService,
) : AbstractInternalStepConfig<Complete, ChangeOrgTeamMemberAccessLevelJourneyState>() {
    override fun mode(state: ChangeOrgTeamMemberAccessLevelJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: ChangeOrgTeamMemberAccessLevelJourneyState) {
        organisationalLandlordUserService.updateRole(state.otherOrganisationalLandlordUserId, state.selectedRole)
        // TODO PDJB-1766: send a confirmation email to the member
    }

    override fun resolveNextDestination(
        state: ChangeOrgTeamMemberAccessLevelJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteChangeAccessLevelStep(
    stepConfig: CompleteChangeAccessLevelStepConfig,
) : JourneyStep.InternalStep<Complete, ChangeOrgTeamMemberAccessLevelJourneyState>(stepConfig)
