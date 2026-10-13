package uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.ChangeOrgTeamMemberAccessLevelJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.ChangeOrgTeamMemberAccessLevelEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService

@JourneyFrameworkComponent
class CompleteChangeAccessLevelStepConfig(
    private val organisationalLandlordUserService: OrganisationalLandlordUserService,
    private val accessChangeEmailSender: EmailNotificationService<ChangeOrgTeamMemberAccessLevelEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) : AbstractInternalStepConfig<Complete, ChangeOrgTeamMemberAccessLevelJourneyState>() {
    override fun mode(state: ChangeOrgTeamMemberAccessLevelJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: ChangeOrgTeamMemberAccessLevelJourneyState) {
        organisationalLandlordUserService.updateRole(state.otherOrganisationalLandlordUserId, state.selectedRole)

        val member =
            organisationalLandlordUserService.getOrganisationalLandlordUserOrNull(state.otherOrganisationalLandlordUserId)
                ?: throw PrsdbWebException(
                    "No organisational landlord user found with id ${state.otherOrganisationalLandlordUserId}",
                )
        val isAdmin = state.selectedRole == OrganisationalLandlordUserRole.ADMIN
        accessChangeEmailSender.sendEmail(
            member.email,
            ChangeOrgTeamMemberAccessLevelEmail(
                memberName = member.name,
                roleName = if (isAdmin) "administrator" else "editor",
                organisationName = member.organisationalLandlord.name,
                isAdmin = isAdmin,
                dashboardUrl = absoluteUrlProvider.buildLandlordDashboardUri(),
            ),
        )
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
