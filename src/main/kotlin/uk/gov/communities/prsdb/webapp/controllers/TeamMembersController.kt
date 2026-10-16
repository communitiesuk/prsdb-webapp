package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.constants.ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.RESEND_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.InviteOrganisationalLandlordUserController.Companion.INVITE_ORGANISATIONAL_LANDLORD_USER_PATH
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.TeamMembersViewModel
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import java.security.Principal

@PreAuthorize("hasRole('ORG_ADMIN')")
@PrsdbController
@RequestMapping(TEAM_MEMBERS_ROUTE)
class TeamMembersController(
    private val userToLandlordService: UserToLandlordService,
    private val organisationalLandlordUserService: OrganisationalLandlordUserService,
    private val organisationalLandlordInvitationService: OrganisationalLandlordInvitationService,
) {
    @GetMapping
    // TODO PDJB-1828: Remove this annotation when the MULTI_USER_ORGANISATIONS flag is removed
    @AvailableWhenFeatureEnabled(MULTI_USER_ORGANISATIONS)
    fun getTeamMembers(
        model: Model,
        principal: Principal,
    ): String {
        val landlord = getCurrentOrganisationalLandlord()

        val teamMembers =
            TeamMembersViewModel.fromMembersAndInvitations(
                organisationalLandlordUserService.getOrganisationalLandlordUsers(landlord),
                organisationalLandlordInvitationService.getInvitationsForOrganisation(landlord),
                principal.name,
            )
        model.addAttribute("teamMembers", teamMembers)
        model.addAttribute("administratorsTabId", ADMINISTRATORS_FRAGMENT)
        model.addAttribute("editorsTabId", EDITORS_FRAGMENT)
        model.addAttribute("invitationsTabId", INVITATIONS_FRAGMENT)
        model.addAttribute("invitationLifetimeInDays", ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS)
        model.addAttribute("inviteTeamMemberUrl", INVITE_ORGANISATIONAL_LANDLORD_USER_PATH)

        return "teamMembers"
    }

    // TODO: PDJB-1060: We should not be using a GET for editing actions. Replace with a confirmation page.
    @GetMapping("$RESEND_PATH_SEGMENT/{invitationId}")
    // TODO PDJB-1828: Remove this annotation when the MULTI_USER_ORGANISATIONS flag is removed
    @AvailableWhenFeatureEnabled(MULTI_USER_ORGANISATIONS)
    fun resendInvitation(
        @PathVariable invitationId: Long,
        redirectAttributes: RedirectAttributes,
    ): String {
        val resentEmail = organisationalLandlordInvitationService.resendInvitation(invitationId, getCurrentOrganisationalLandlord())
        if (resentEmail != null) {
            redirectAttributes.addFlashAttribute("resendInvitationEmail", resentEmail)
        }
        return "redirect:$TEAM_MEMBERS_ROUTE#$INVITATIONS_FRAGMENT"
    }

    private fun getCurrentOrganisationalLandlord(): OrganisationalLandlord =
        userToLandlordService.getCurrentLandlordForUser() as? OrganisationalLandlord
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only organisation landlords can manage team members")

    companion object {
        const val TEAM_MEMBERS_PATH_SEGMENT = "team-members"
        const val TEAM_MEMBERS_ROUTE = "/$LANDLORD_PATH_SEGMENT/$TEAM_MEMBERS_PATH_SEGMENT"
        const val ADMINISTRATORS_FRAGMENT = "administrators"
        const val EDITORS_FRAGMENT = "editors"
        const val INVITATIONS_FRAGMENT = "invitations"

        fun getResendInvitationPath(invitationId: Long): String = "$TEAM_MEMBERS_ROUTE/$RESEND_PATH_SEGMENT/$invitationId"
    }
}
