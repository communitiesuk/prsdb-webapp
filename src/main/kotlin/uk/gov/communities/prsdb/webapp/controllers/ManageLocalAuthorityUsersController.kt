package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.ROLE_LA_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_LA_USER
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityDashboardController.Companion.LOCAL_AUTHORITY_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.ConfirmedEmailRequestModel
import uk.gov.communities.prsdb.webapp.models.requestModels.LocalAuthorityUserAccessLevelRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.PaginationViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalAuthorityInvitationCancellationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalAuthorityInvitationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import java.security.Principal

@PreAuthorize("hasAnyRole('LA_ADMIN', 'SYSTEM_OPERATOR')")
@Controller
@RequestMapping("/$LOCAL_AUTHORITY_PATH_SEGMENT/{localAuthorityId}")
class ManageLocalAuthorityUsersController(
    var invitationEmailSender: EmailNotificationService<LocalAuthorityInvitationEmail>,
    var cancellationEmailSender: EmailNotificationService<LocalAuthorityInvitationCancellationEmail>,
    var invitationService: LocalAuthorityInvitationService,
    val localAuthorityDataService: LocalAuthorityDataService,
    val absoluteUrlProvider: AbsoluteUrlProvider,
    val localAuthorityService: LocalAuthorityService,
    val securityContextService: SecurityContextService,
) {
    @GetMapping("/manage-users")
    fun index(
        @PathVariable localAuthorityId: Int,
        model: Model,
        principal: Principal,
        @RequestParam(value = "page", required = false) @Min(1) page: Int = 1,
        request: HttpServletRequest,
    ): String {
        val loggedInLaAdmin = getCurrentUserIfTheyAreAnLAAdminForTheCurrentLA(principal, localAuthorityId, request)

        val localAuthority = getLocalAuthority(principal, localAuthorityId, request)

        val pagedUserList =
            localAuthorityDataService.getPaginatedUsersAndInvitations(
                localAuthority,
                page - 1,
                filterOutLaAdminInvitations = !request.isUserInRole(ROLE_SYSTEM_OPERATOR),
            )

        if (pagedUserList.totalPages != 0 && pagedUserList.totalPages < page) {
            return "redirect:/local-authority/{localAuthorityId}/manage-users"
        }

        model.addAttribute("currentUserId", loggedInLaAdmin?.id)
        model.addAttribute("localAuthority", localAuthority)
        model.addAttribute("userList", pagedUserList)
        model.addAttribute(
            "paginationViewModel",
            PaginationViewModel(page, pagedUserList.totalPages, request),
        )
        model.addAttribute("userCanEditTheirOwnAccount", request.isUserInRole(ROLE_SYSTEM_OPERATOR))

        // TODO: PRSD-672 - if the user is not an la admin, make this a link to the system operator dashboard
        model.addAttribute("dashboardUrl", LOCAL_AUTHORITY_DASHBOARD_URL)

        return "manageLAUsers"
    }

    @GetMapping("/edit-user/{localAuthorityUserId}")
    fun getEditUserAccessLevelPage(
        @PathVariable localAuthorityId: Int,
        @PathVariable localAuthorityUserId: Long,
        principal: Principal,
        model: Model,
        request: HttpServletRequest,
    ): String {
        throwErrorIfNonSystemOperatorIsUpdatingTheirOwnAccount(principal, localAuthorityId, localAuthorityUserId, request)

        val localAuthorityUser =
            localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(localAuthorityUserId, localAuthorityId)

        model.addAttribute("backLinkPath", "../manage-users")
        model.addAttribute("localAuthorityUser", localAuthorityUser)
        model.addAttribute(
            "options",
            listOf(
                RadiosButtonViewModel(
                    false,
                    "basic",
                    "editLAUserAccess.radios.option.basic.label",
                    "editLAUserAccess.radios.option.basic.hint",
                ),
                RadiosButtonViewModel(
                    true,
                    "admin",
                    "editLAUserAccess.radios.option.admin.label",
                    "editLAUserAccess.radios.option.admin.hint",
                ),
            ),
        )

        return "editLAUserAccess"
    }

    @PostMapping("/edit-user/{localAuthorityUserId}")
    fun updateUserAccessLevel(
        @PathVariable localAuthorityId: Int,
        @PathVariable localAuthorityUserId: Long,
        @ModelAttribute localAuthorityUserAccessLevel: LocalAuthorityUserAccessLevelRequestModel,
        principal: Principal,
        request: HttpServletRequest,
    ): String {
        throwErrorIfNonSystemOperatorIsUpdatingTheirOwnAccount(principal, localAuthorityId, localAuthorityUserId, request)

        localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(localAuthorityUserId, localAuthorityId)

        localAuthorityDataService.updateUserAccessLevel(localAuthorityUserAccessLevel, localAuthorityUserId)
        return "redirect:/local-authority/{localAuthorityId}/manage-users"
    }

    @GetMapping("/delete-user/{localAuthorityUserId}")
    fun confirmDeleteUser(
        @PathVariable localAuthorityId: Int,
        @PathVariable localAuthorityUserId: Long,
        model: Model,
        principal: Principal,
        request: HttpServletRequest,
    ): String {
        throwErrorIfNonSystemOperatorIsUpdatingTheirOwnAccount(principal, localAuthorityId, localAuthorityUserId, request)

        val userToDelete =
            localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(localAuthorityUserId, localAuthorityId)
        model.addAttribute("user", userToDelete)
        model.addAttribute("backLinkPath", "../edit-user/$localAuthorityUserId")
        return "deleteLAUser"
    }

    @PostMapping("/delete-user/{localAuthorityUserId}")
    fun deleteUser(
        @PathVariable localAuthorityId: Int,
        @PathVariable localAuthorityUserId: Long,
        principal: Principal,
        redirectAttributes: RedirectAttributes,
        request: HttpServletRequest,
    ): String {
        throwErrorIfNonSystemOperatorIsUpdatingTheirOwnAccount(principal, localAuthorityId, localAuthorityUserId, request)
        val user = localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(localAuthorityUserId, localAuthorityId)

        if (request.isUserInRole(ROLE_SYSTEM_OPERATOR) &&
            (request.isUserInRole(ROLE_LA_ADMIN) || request.isUserInRole(ROLE_LA_USER))
        ) {
            // If the user is a system operator they can delete themself from the local_authority_user table
            // If this happens we will need to update their user roles as the Manage LA Users page
            // will throw an error if they have the LA_ADMIN role but are no longer in the local_authority_users table.
            val currentUser = localAuthorityDataService.getLocalAuthorityUser(principal.name)
            if (currentUser.id == user.id) {
                redirectAttributes.addFlashAttribute("currentUserDeletedThemself", true)
            }
        }

        localAuthorityDataService.deleteUser(localAuthorityUserId)

        redirectAttributes.addFlashAttribute("deletedUserName", user.userName)
        return "redirect:../delete-user/success"
    }

    @GetMapping("/delete-user/success")
    fun deleteUserSuccess(
        @PathVariable localAuthorityId: Int,
        model: Model,
        principal: Principal,
        request: HttpServletRequest,
    ): String {
        model.addAttribute("localAuthority", getLocalAuthority(principal, localAuthorityId, request))

        if (model.getAttribute("currentUserDeletedThemself") == true) {
            // This will only update the roles of the current user so is only needed if they have deleted themself.
            securityContextService.refreshContext()
        }
        return "deleteLAUserSuccess"
    }

    @GetMapping("/invite-new-user")
    fun inviteNewUser(
        @PathVariable localAuthorityId: Int,
        model: Model,
        principal: Principal,
        request: HttpServletRequest,
    ): String {
        model.addAttribute("councilName", getLocalAuthority(principal, localAuthorityId, request).name)
        model.addAttribute("confirmedEmailRequestModel", ConfirmedEmailRequestModel())

        return "inviteLAUser"
    }

    @PostMapping("/invite-new-user", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun sendInvitation(
        @PathVariable localAuthorityId: Int,
        model: Model,
        @Valid
        @ModelAttribute
        emailModel: ConfirmedEmailRequestModel,
        bindingResult: BindingResult,
        principal: Principal,
        redirectAttributes: RedirectAttributes,
        request: HttpServletRequest,
    ): String {
        val currentAuthority = getLocalAuthority(principal, localAuthorityId, request)
        model.addAttribute("councilName", currentAuthority.name)

        if (bindingResult.hasErrors()) {
            return "inviteLAUser"
        }

        try {
            val token = invitationService.createInvitationToken(emailModel.email, currentAuthority)
            val invitationLinkAddress = absoluteUrlProvider.buildInvitationUri(token)
            invitationEmailSender.sendEmail(
                emailModel.email,
                LocalAuthorityInvitationEmail(currentAuthority, invitationLinkAddress),
            )

            redirectAttributes.addFlashAttribute("invitedEmailAddress", emailModel.email)
            return "redirect:invite-new-user/success"
        } catch (retryException: TransientEmailSentException) {
            bindingResult.reject("addLAUser.error.retryable")
            return "inviteLAUser"
        }
    }

    @GetMapping("/invite-new-user/success")
    fun successInvitedNewUser(
        @PathVariable localAuthorityId: Int,
        principal: Principal,
        model: Model,
        request: HttpServletRequest,
    ): String {
        model.addAttribute("localAuthority", getLocalAuthority(principal, localAuthorityId, request))
        model.addAttribute("dashboardUrl", LOCAL_AUTHORITY_DASHBOARD_URL)
        return "inviteLAUserSuccess"
    }

    @GetMapping("/cancel-invitation/{invitationId}")
    fun confirmCancelInvitation(
        @PathVariable localAuthorityId: Int,
        @PathVariable invitationId: Long,
        principal: Principal,
        model: Model,
        request: HttpServletRequest,
    ): String {
        val invitation = invitationService.getInvitationById(invitationId)

        val authority = getLocalAuthority(principal, localAuthorityId, request)

        if (authority.id != invitation.invitingAuthority.id) {
            throw AccessDeniedException(
                "A user on the Manage LA Users page for ${authority.name} tried to cancel an invitation " +
                    "from LA ${invitation.invitingAuthority.name}",
            )
        }

        model.addAttribute("backLinkPath", "../manage-users")
        model.addAttribute("email", invitation.invitedEmail)

        return "cancelLAUserInvitation"
    }

    @PostMapping("/cancel-invitation/{invitationId}")
    fun cancelInvitation(
        @PathVariable localAuthorityId: Int,
        @PathVariable invitationId: Long,
        redirectAttributes: RedirectAttributes,
    ): String {
        val invitation = invitationService.getInvitationById(invitationId)
        invitationService.deleteInvitation(invitationId)

        cancellationEmailSender.sendEmail(
            invitation.invitedEmail,
            LocalAuthorityInvitationCancellationEmail(invitation.invitingAuthority),
        )

        redirectAttributes.addFlashAttribute("deletedEmail", invitation.invitedEmail)
        redirectAttributes.addFlashAttribute("localAuthority", invitation.invitingAuthority)
        return "redirect:../cancel-invitation/success"
    }

    @GetMapping("/cancel-invitation/success")
    fun cancelInvitationSuccess(
        @PathVariable localAuthorityId: String,
        model: Model,
    ): String = "cancelLAUserInvitationSuccess"

    private fun throwErrorIfNonSystemOperatorIsUpdatingTheirOwnAccount(
        principal: Principal,
        localAuthorityId: Int,
        localAuthorityUserId: Long,
        request: HttpServletRequest,
    ) {
        if (!request.isUserInRole(ROLE_SYSTEM_OPERATOR)) {
            val (currentUser, _) =
                localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                    localAuthorityId,
                    principal.name,
                )
            if (currentUser.id == localAuthorityUserId) {
                throw AccessDeniedException("Local authority users cannot edit their own accounts; another admin must do so")
            }
        }
    }

    private fun getLocalAuthority(
        principal: Principal,
        localAuthorityId: Int,
        request: HttpServletRequest,
    ): LocalAuthority =
        if (request.isUserInRole(ROLE_SYSTEM_OPERATOR)) {
            localAuthorityService.retrieveLocalAuthorityById(localAuthorityId)
        } else {
            val laUserAndla =
                localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                    localAuthorityId,
                    principal.name,
                )
            laUserAndla.second
        }

    private fun getCurrentUserIfTheyAreAnLAAdminForTheCurrentLA(
        principal: Principal,
        localAuthorityId: Int,
        request: HttpServletRequest,
    ): LocalAuthorityUserDataModel? {
        if (!request.isUserInRole(ROLE_LA_ADMIN)) {
            return null
        }
        try {
            val (currentUser, _) =
                localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                    localAuthorityId,
                    principal.name,
                )
            return currentUser
        } catch (exception: AccessDeniedException) {
            // This is expected if the user is not an admin for the current LA
            return null
        }
    }
}
