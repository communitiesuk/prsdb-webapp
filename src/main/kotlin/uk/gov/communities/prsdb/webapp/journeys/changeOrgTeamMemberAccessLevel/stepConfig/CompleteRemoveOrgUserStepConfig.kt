package uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.ChangeOrgTeamMemberAccessLevelJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.RemoveOrgTeamMemberConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.RemoveOrgTeamMemberNotificationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import uk.gov.communities.prsdb.webapp.services.UserToOrgLandlordUserService

@JourneyFrameworkComponent
class CompleteRemoveOrgUserStepConfig(
    private val organisationalLandlordUserService: OrganisationalLandlordUserService,
    private val userToOrgLandlordUserService: UserToOrgLandlordUserService,
    private val removalConfirmationEmailSender: EmailNotificationService<RemoveOrgTeamMemberConfirmationEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val removalNotificationEmailSender: EmailNotificationService<RemoveOrgTeamMemberNotificationEmail>,
) : AbstractInternalStepConfig<Complete, ChangeOrgTeamMemberAccessLevelJourneyState>() {
    override fun mode(state: ChangeOrgTeamMemberAccessLevelJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: ChangeOrgTeamMemberAccessLevelJourneyState) {
        val removingAdmin =
            userToOrgLandlordUserService.getOrganisationalLandlordUserForCurrentUser()
                ?: throw PrsdbWebException(
                    "Current user ${SecurityContextHolder.getContext().authentication!!.name} is not an organisational landlord user",
                )
        val removedMember =
            organisationalLandlordUserService.getOrganisationalLandlordUserOrNull(state.otherOrganisationalLandlordUserId)
                ?: throw PrsdbWebException(
                    "No organisational landlord user found with id ${state.otherOrganisationalLandlordUserId}",
                )
        val removedConfirmationEmailModel =
            RemoveOrgTeamMemberConfirmationEmail(
                adminName = removingAdmin.name,
                memberName = removedMember.name,
                organisationName = removedMember.organisationalLandlord.name,
                teamMembersUrl = absoluteUrlProvider.buildTeamMembersUri(),
            )
        val removedNotificationEmailModel =
            RemoveOrgTeamMemberNotificationEmail(
                memberName = removedMember.name,
                organisationName = removedMember.organisationalLandlord.name,
            )

        organisationalLandlordUserService.removeOrganisationalLandlordUser(state.otherOrganisationalLandlordUserId)

        removalConfirmationEmailSender.sendEmail(removingAdmin.email, removedConfirmationEmailModel)
        removalNotificationEmailSender.sendEmail(removedMember.email, removedNotificationEmailModel)

        // TODO PDJB-1873: Show a success banner after removing the user
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
