package uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
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
        val memberName =
            organisationalLandlordUserService
                .getOrganisationalLandlordUserOrNull(state.otherOrganisationalLandlordUserId)
                ?.name
                ?: throw PrsdbWebException(
                    "Could not determine the name of organisational landlord user ${state.otherOrganisationalLandlordUserId}",
                )
        val contentKey =
            when (state.selectedRole) {
                OrganisationalLandlordUserRole.ADMIN -> "teamMembers.accessLevelChangedBanner.content.administrator"
                OrganisationalLandlordUserRole.EDITOR -> "teamMembers.accessLevelChangedBanner.content.editor"
            }
        state.deleteJourney()
        return (defaultDestination as Destination.ExternalUrl)
            .withFlashAttribute("accessLevelChangedMemberName", memberName)
            .withFlashAttribute("accessLevelChangedBannerContentKey", contentKey)
    }
}

@JourneyFrameworkComponent
class CompleteChangeAccessLevelStep(
    stepConfig: CompleteChangeAccessLevelStepConfig,
) : JourneyStep.InternalStep<Complete, ChangeOrgTeamMemberAccessLevelJourneyState>(stepConfig)
