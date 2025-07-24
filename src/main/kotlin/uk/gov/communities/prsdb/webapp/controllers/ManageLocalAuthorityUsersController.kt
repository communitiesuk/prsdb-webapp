package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CANCEL_INVITATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DELETE_USER_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.EDIT_USER_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.INVITE_NEW_USER_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MANAGE_USERS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.ROLE_LA_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_LA_USER
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.constants.SUCCESS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityDashboardController.Companion.LOCAL_AUTHORITY_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController.Companion.LOCAL_AUTHORITY_ROUTE
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
@PrsdbController
@RequestMapping(LOCAL_AUTHORITY_ROUTE)
class ManageLocalAuthorityUsersController(
    var invitationEmailSender: EmailNotificationService<LocalAuthorityInvitationEmail>,
    var cancellationEmailSender: EmailNotificationService<LocalAuthorityInvitationCancellationEmail>,
    var invitationService: LocalAuthorityInvitationService,
    val localAuthorityDataService: LocalAuthorityDataService,
    val absoluteUrlProvider: AbsoluteUrlProvider,
    val localAuthorityService: LocalAuthorityService,
    val securityContextService: SecurityContextService,
) {
    @GetMapping("/$MANAGE_USERS_PATH_SEGMENT")
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
            return "redirect:${getLaManageUsersRoute(localAuthorityId)}"
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

    @GetMapping("/$EDIT_USER_ROUTE")
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

        model.addAttribute("backLinkPath", "../$MANAGE_USERS_PATH_SEGMENT")
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

    @PostMapping("/$EDIT_USER_ROUTE")
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
        return "redirect:${getLaManageUsersRoute(localAuthorityId)}"
    }

    @GetMapping("/$DELETE_USER_ROUTE")
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
        model.addAttribute("backLinkPath", "../$EDIT_USER_PATH_SEGMENT/$localAuthorityUserId")
        return "deleteLAUser"
    }

    @PostMapping("/$DELETE_USER_ROUTE")
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
        return "redirect:../$DELETE_USER_PATH_SEGMENT/$SUCCESS_PATH_SEGMENT"
    }

    @GetMapping("/$DELETE_USER_PATH_SEGMENT/$SUCCESS_PATH_SEGMENT")
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

    @GetMapping("/$INVITE_NEW_USER_PATH_SEGMENT")
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

    @PostMapping("/$INVITE_NEW_USER_PATH_SEGMENT", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
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
                LocalAuthorityInvitationEmail(
                    currentAuthority,
                    invitationLinkAddress,
                    absoluteUrlProvider.buildLocalAuthorityDashboardUri().toString(),
                ),
            )

            redirectAttributes.addFlashAttribute("invitedEmailAddress", emailModel.email)
            return "redirect:$INVITE_NEW_USER_PATH_SEGMENT/$SUCCESS_PATH_SEGMENT"
        } catch (retryException: TransientEmailSentException) {
            bindingResult.reject("addLAUser.error.retryable")
            return "inviteLAUser"
        }
    }

    @GetMapping("/$INVITE_NEW_USER_PATH_SEGMENT/$SUCCESS_PATH_SEGMENT")
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

    @GetMapping("/$CANCEL_INVITE_ROUTE")
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

        model.addAttribute("backLinkPath", "../$MANAGE_USERS_PATH_SEGMENT")
        model.addAttribute("email", invitation.invitedEmail)

        return "cancelLAUserInvitation"
    }

    @PostMapping("/$CANCEL_INVITE_ROUTE")
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
        return "redirect:../$CANCEL_INVITATION_PATH_SEGMENT/$SUCCESS_PATH_SEGMENT"
    }

    @GetMapping("/$CANCEL_INVITATION_PATH_SEGMENT/$SUCCESS_PATH_SEGMENT")
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

    companion object {
        const val LOCAL_AUTHORITY_ROUTE = "/$LOCAL_AUTHORITY_PATH_SEGMENT/{localAuthorityId}"
        const val EDIT_USER_ROUTE = "$EDIT_USER_PATH_SEGMENT/{localAuthorityUserId}"
        const val DELETE_USER_ROUTE = "$DELETE_USER_PATH_SEGMENT/{localAuthorityUserId}"
        const val CANCEL_INVITE_ROUTE = "$CANCEL_INVITATION_PATH_SEGMENT/{invitationId}"

        private const val LA_MANAGE_USERS_ROUTE = "$LOCAL_AUTHORITY_ROUTE/$MANAGE_USERS_PATH_SEGMENT"
        private const val LA_EDIT_USER_ROUTE = "$LOCAL_AUTHORITY_ROUTE/$EDIT_USER_ROUTE"
        private const val LA_DELETE_USER_ROUTE = "$LOCAL_AUTHORITY_ROUTE/$DELETE_USER_ROUTE"
        private const val LA_DELETE_USER_SUCCESS_ROUTE = "$LOCAL_AUTHORITY_ROUTE/$DELETE_USER_PATH_SEGMENT/$SUCCESS_PATH_SEGMENT"
        private const val LA_INVITE_NEW_USER_ROUTE = "$LOCAL_AUTHORITY_ROUTE/$INVITE_NEW_USER_PATH_SEGMENT"
        private const val LA_CANCEL_INVITE_ROUTE = "$LOCAL_AUTHORITY_ROUTE/$CANCEL_INVITE_ROUTE"

        fun getLaManageUsersRoute(localAuthorityId: Int): String =
            UriTemplate(LA_MANAGE_USERS_ROUTE).expand(localAuthorityId).toASCIIString()

        fun getLaEditUserRoute(
            localAuthorityId: Int,
            localAuthorityUserId: Long,
        ): String = UriTemplate(LA_EDIT_USER_ROUTE).expand(localAuthorityId, localAuthorityUserId).toASCIIString()

        fun getLaDeleteUserRoute(
            localAuthorityId: Int,
            localAuthorityUserId: Long,
        ): String = UriTemplate(LA_DELETE_USER_ROUTE).expand(localAuthorityId, localAuthorityUserId).toASCIIString()

        fun getLaDeleteUserSuccessRoute(localAuthorityId: Int): String =
            UriTemplate(LA_DELETE_USER_SUCCESS_ROUTE).expand(localAuthorityId).toASCIIString()

        fun getLaInviteNewUserRoute(localAuthorityId: Int): String =
            UriTemplate(LA_INVITE_NEW_USER_ROUTE).expand(localAuthorityId).toASCIIString()

        fun getLaCancelInviteRoute(
            localAuthorityId: Int,
            localAuthorityUserId: Long,
        ): String = UriTemplate(LA_CANCEL_INVITE_ROUTE).expand(localAuthorityId, localAuthorityUserId).toASCIIString()
    }
}
