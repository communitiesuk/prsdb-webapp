package uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.ChangeOrgTeamMemberAccessLevelJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService

@JourneyFrameworkComponent
class CompleteRemoveOrgUserStepConfig(
    private val organisationalLandlordUserService: OrganisationalLandlordUserService,
) : AbstractInternalStepConfig<Complete, ChangeOrgTeamMemberAccessLevelJourneyState>() {
    override fun mode(state: ChangeOrgTeamMemberAccessLevelJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: ChangeOrgTeamMemberAccessLevelJourneyState) {
        state.removedMemberName = organisationalLandlordUserService
            .getOrganisationalLandlordUserOrNull(state.otherOrganisationalLandlordUserId)
            ?.name
            ?: throw PrsdbWebException(
                "Could not determine the name of organisational landlord user ${state.otherOrganisationalLandlordUserId}",
            )

        organisationalLandlordUserService.removeOrganisationalLandlordUser(state.otherOrganisationalLandlordUserId)

        // TODO PDJB-1768: Email the removed user
        // TODO PDJB-1767: Email the admin who removed the user
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
class CompleteRemoveOrgUserStep(
    stepConfig: CompleteRemoveOrgUserStepConfig,
) : JourneyStep.InternalStep<Complete, ChangeOrgTeamMemberAccessLevelJourneyState>(stepConfig)
