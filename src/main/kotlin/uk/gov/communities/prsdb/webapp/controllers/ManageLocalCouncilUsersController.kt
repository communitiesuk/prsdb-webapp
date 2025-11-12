package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
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
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CANCEL_INVITATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DELETE_USER_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.EDIT_USER_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.INVITE_NEW_USER_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MANAGE_USERS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_USER
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.constants.VOWELS
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilDashboardController.Companion.LOCAL_AUTHORITY_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController.Companion.LOCAL_AUTHORITY_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalCouncilUserDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.ConfirmedEmailRequestModel
import uk.gov.communities.prsdb.webapp.models.requestModels.LocalCouncilUserAccessLevelRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.PaginationViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilInvitationCancellationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilInvitationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilDataService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import java.security.Principal

@PreAuthorize("hasAnyRole('LA_ADMIN', 'SYSTEM_OPERATOR')")
@PrsdbController
@RequestMapping(LOCAL_AUTHORITY_ROUTE)
class ManageLocalCouncilUsersController(
    var invitationEmailSender: EmailNotificationService<LocalCouncilInvitationEmail>,
    var cancellationEmailSender: EmailNotificationService<LocalCouncilInvitationCancellationEmail>,
    var invitationService: LocalCouncilInvitationService,
    val localCouncilDataService: LocalCouncilDataService,
    val absoluteUrlProvider: AbsoluteUrlProvider,
    val localCouncilService: LocalCouncilService,
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
            localCouncilDataService.getPaginatedUsersAndInvitations(
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

        return "manageLocalCouncilUsers"
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
            localCouncilDataService.getLocalAuthorityUserIfAuthorizedLA(localAuthorityUserId, localAuthorityId)

        model.addAttribute("backUrl", "../$MANAGE_USERS_PATH_SEGMENT")
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
        model.addAttribute("deleteUserUrl", getLaDeleteUserRoute(localAuthorityId, localAuthorityUserId))

        return "editLocalCouncilUserAccess"
    }

    @PostMapping("/$EDIT_USER_ROUTE")
    fun updateUserAccessLevel(
        @PathVariable localAuthorityId: Int,
        @PathVariable localAuthorityUserId: Long,
        @ModelAttribute localAuthorityUserAccessLevel: LocalCouncilUserAccessLevelRequestModel,
        principal: Principal,
        request: HttpServletRequest,
    ): String {
        throwErrorIfNonSystemOperatorIsUpdatingTheirOwnAccount(principal, localAuthorityId, localAuthorityUserId, request)

        localCouncilDataService.getLocalAuthorityUserIfAuthorizedLA(localAuthorityUserId, localAuthorityId)

        localCouncilDataService.updateUserAccessLevel(localAuthorityUserAccessLevel, localAuthorityUserId)
        return "redirect:${getLaManageUsersRoute(localAuthorityId)}"
    }

    @GetMapping("/$DELETE_USER_ROUTE")
    fun confirmDeleteUser(
        @PathVariable localAuthorityId: Int,
        @PathVariable deleteeId: Long,
        model: Model,
        principal: Principal,
        request: HttpServletRequest,
    ): String {
        throwErrorIfNonSystemOperatorIsUpdatingTheirOwnAccount(principal, localAuthorityId, deleteeId, request)

        val userToDelete =
            localCouncilDataService.getLocalAuthorityUserIfAuthorizedLA(deleteeId, localAuthorityId)
        model.addAttribute("user", userToDelete)
        model.addAttribute("backLinkPath", "../$EDIT_USER_PATH_SEGMENT/$deleteeId")
        return "deleteLocalCouncilUser"
    }

    @PostMapping("/$DELETE_USER_ROUTE")
    fun deleteUser(
        @PathVariable localAuthorityId: Int,
        @PathVariable deleteeId: Long,
        principal: Principal,
        redirectAttributes: RedirectAttributes,
        request: HttpServletRequest,
    ): String {
        throwErrorIfNonSystemOperatorIsUpdatingTheirOwnAccount(principal, localAuthorityId, deleteeId, request)
        val userBeingDeleted = localCouncilDataService.getLocalAuthorityUserIfAuthorizedLA(deleteeId, localAuthorityId)

        localCouncilDataService.deleteUser(userBeingDeleted)

        if (request.isUserInRole(ROLE_SYSTEM_OPERATOR) &&
            (request.isUserInRole(ROLE_LOCAL_COUNCIL_ADMIN) || request.isUserInRole(ROLE_LOCAL_COUNCIL_USER))
        ) {
            // If the user is a system operator they can delete themself from the local_authority_user table
            // If this happens we will need to update their user roles as the Manage LA Users page
            // will throw an error if they have the LA_ADMIN role but are no longer in the local_authority_users table.
            val currentUser = localCouncilDataService.getLocalAuthorityUser(principal.name)
            if (currentUser.id == userBeingDeleted.id) {
                securityContextService.refreshContext()
            }
        }

        localCouncilDataService.addDeletedUserToSession(userBeingDeleted)
        return "redirect:../$DELETE_USER_CONFIRMATION_ROUTE"
    }

    @GetMapping("/$DELETE_USER_CONFIRMATION_ROUTE")
    fun deleteUserSuccess(
        @PathVariable localAuthorityId: Int,
        @PathVariable deleteeId: Long,
        model: Model,
        principal: Principal,
        request: HttpServletRequest,
    ): String {
        val userDeletedThisSession = localCouncilDataService.getUserDeletedThisSessionById(deleteeId)

        model.addAttribute("deletedUserName", userDeletedThisSession.name)

        model.addAttribute("localAuthority", getLocalAuthority(principal, localAuthorityId, request))

        model.addAttribute("returnToManageUsersUrl", getLaManageUsersRoute(localAuthorityId))

        return "deleteLocalCouncilUserSuccess"
    }

    @GetMapping("/$INVITE_NEW_USER_PATH_SEGMENT")
    fun inviteNewUser(
        @PathVariable localAuthorityId: Int,
        model: Model,
        principal: Principal,
        request: HttpServletRequest,
    ): String {
        val councilName = getLocalAuthority(principal, localAuthorityId, request).name
        val councilNameBeginsWithVowel = councilName[0].uppercase() in VOWELS

        model.addAttribute("councilName", councilName)
        model.addAttribute("councilNameBeginsWithVowel", councilNameBeginsWithVowel)
        model.addAttribute("confirmedEmailRequestModel", ConfirmedEmailRequestModel())

        return "inviteLocalCouncilUser"
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
            return "inviteLocalCouncilUser"
        }

        try {
            val token = invitationService.createInvitationToken(emailModel.email, currentAuthority)
            val invitationLinkAddress = absoluteUrlProvider.buildInvitationUri(token)
            invitationEmailSender.sendEmail(
                emailModel.email,
                LocalCouncilInvitationEmail(
                    currentAuthority,
                    invitationLinkAddress,
                    absoluteUrlProvider.buildLocalAuthorityDashboardUri().toString(),
                ),
            )
            localCouncilDataService.sendUserInvitedEmailsToAdmins(
                currentAuthority,
                emailModel.email,
            )

            localCouncilDataService.addInvitedLocalAuthorityUserToSession(localAuthorityId, emailModel.email)

            return "redirect:$INVITE_USER_CONFIRMATION_ROUTE"
        } catch (retryException: TransientEmailSentException) {
            bindingResult.reject("addLAUser.error.retryable")
            return "inviteLocalCouncilUser"
        }
    }

    @GetMapping("/$INVITE_USER_CONFIRMATION_ROUTE")
    fun inviteNewUserConfirmation(
        @PathVariable localAuthorityId: Int,
        principal: Principal,
        model: Model,
        request: HttpServletRequest,
    ): String {
        val invitedEmail =
            localCouncilDataService.getLastLocalAuthorityUserInvitedThisSession(localAuthorityId)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No email address found in the session for a user invited to local authority with id $localAuthorityId",
                )

        model.addAttribute("localAuthority", getLocalAuthority(principal, localAuthorityId, request))
        model.addAttribute("dashboardUrl", LOCAL_AUTHORITY_DASHBOARD_URL)
        model.addAttribute("invitedEmailAddress", invitedEmail)
        return "inviteLocalCouncilUserSuccess"
    }

    @GetMapping("/$CANCEL_INVITE_ROUTE")
    fun confirmCancelInvitation(
        @PathVariable localAuthorityId: Int,
        @PathVariable invitationId: Long,
        principal: Principal,
        model: Model,
        request: HttpServletRequest,
    ): String {
        val invitation =
            invitationService.getInvitationByIdOrNull(invitationId) ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Invitation with id $invitationId was not found in the local_authority_invitations table",
            )

        val authority = getLocalAuthority(principal, localAuthorityId, request)

        if (authority.id != invitation.invitingAuthority.id) {
            throw AccessDeniedException(
                "A user on the Manage LA Users page for ${authority.name} tried to cancel an invitation " +
                    "from LA ${invitation.invitingAuthority.name}",
            )
        }

        model.addAttribute("backLinkPath", "../$MANAGE_USERS_PATH_SEGMENT")
        model.addAttribute("email", invitation.invitedEmail)

        return "cancelLocalCouncilUserInvitation"
    }

    @PostMapping("/$CANCEL_INVITE_ROUTE")
    fun cancelInvitation(
        @PathVariable localAuthorityId: Int,
        @PathVariable invitationId: Long,
        redirectAttributes: RedirectAttributes,
    ): String {
        val invitation =
            invitationService.getInvitationByIdOrNull(invitationId) ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Invitation with id $invitationId was not found in the local_authority_invitations table",
            )

        invitationService.deleteInvitation(invitationId)

        cancellationEmailSender.sendEmail(
            invitation.invitedEmail,
            LocalCouncilInvitationCancellationEmail(invitation.invitingAuthority),
        )

        localCouncilDataService.addCancelledInvitationToSession(
            invitation,
        )

        return "redirect:../$CANCEL_INVITE_CONFIRMATION_ROUTE"
    }

    @GetMapping("/$CANCEL_INVITE_CONFIRMATION_ROUTE")
    fun cancelInvitationSuccess(
        @PathVariable localAuthorityId: Int,
        @PathVariable invitationId: Long,
        model: Model,
        principal: Principal,
        request: HttpServletRequest,
    ): String {
        val invitationDeletedThisSession = localCouncilDataService.getInvitationCancelledThisSessionById(invitationId)

        model.addAttribute("deletedEmail", invitationDeletedThisSession.invitedEmail)

        model.addAttribute("localAuthority", getLocalAuthority(principal, localAuthorityId, request))
        model.addAttribute("returnToManageUsersUrl", getLaManageUsersRoute(localAuthorityId))

        return "cancelLocalCouncilUserInvitationSuccess"
    }

    private fun throwErrorIfNonSystemOperatorIsUpdatingTheirOwnAccount(
        principal: Principal,
        localAuthorityId: Int,
        localAuthorityUserId: Long,
        request: HttpServletRequest,
    ) {
        if (!request.isUserInRole(ROLE_SYSTEM_OPERATOR)) {
            val (currentUser, _) =
                localCouncilDataService.getUserAndLocalAuthorityIfAuthorizedUser(
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
    ): LocalCouncil =
        if (request.isUserInRole(ROLE_SYSTEM_OPERATOR)) {
            localCouncilService.retrieveLocalAuthorityById(localAuthorityId)
        } else {
            val laUserAndla =
                localCouncilDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                    localAuthorityId,
                    principal.name,
                )
            laUserAndla.second
        }

    private fun getCurrentUserIfTheyAreAnLAAdminForTheCurrentLA(
        principal: Principal,
        localAuthorityId: Int,
        request: HttpServletRequest,
    ): LocalCouncilUserDataModel? {
        if (!request.isUserInRole(ROLE_LOCAL_COUNCIL_ADMIN)) {
            return null
        }
        try {
            val (currentUser, _) =
                localCouncilDataService.getUserAndLocalAuthorityIfAuthorizedUser(
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
        const val LOCAL_AUTHORITY_ROUTE = "/$LOCAL_COUNCIL_PATH_SEGMENT/{localAuthorityId}"
        const val EDIT_USER_ROUTE = "$EDIT_USER_PATH_SEGMENT/{localAuthorityUserId}"
        const val DELETE_USER_ROUTE = "$DELETE_USER_PATH_SEGMENT/{deleteeId}"
        const val DELETE_USER_CONFIRMATION_ROUTE = "$DELETE_USER_ROUTE/$CONFIRMATION_PATH_SEGMENT"
        const val INVITE_USER_CONFIRMATION_ROUTE = "$INVITE_NEW_USER_PATH_SEGMENT/$CONFIRMATION_PATH_SEGMENT"
        const val CANCEL_INVITE_ROUTE = "$CANCEL_INVITATION_PATH_SEGMENT/{invitationId}"
        const val CANCEL_INVITE_CONFIRMATION_ROUTE = "$CANCEL_INVITE_ROUTE/$CONFIRMATION_PATH_SEGMENT"

        private const val LA_MANAGE_USERS_ROUTE = "$LOCAL_AUTHORITY_ROUTE/$MANAGE_USERS_PATH_SEGMENT"
        private const val LA_EDIT_USER_ROUTE = "$LOCAL_AUTHORITY_ROUTE/$EDIT_USER_ROUTE"
        private const val LA_DELETE_USER_ROUTE = "$LOCAL_AUTHORITY_ROUTE/$DELETE_USER_ROUTE"
        private const val LA_DELETE_USER_CONFIRMATION_ROUTE = "$LOCAL_AUTHORITY_ROUTE/$DELETE_USER_CONFIRMATION_ROUTE"
        private const val LA_INVITE_NEW_USER_ROUTE = "$LOCAL_AUTHORITY_ROUTE/$INVITE_NEW_USER_PATH_SEGMENT"
        private const val LA_INVITE_NEW_USER_CONFIRMATION_ROUTE = "$LOCAL_AUTHORITY_ROUTE/$INVITE_USER_CONFIRMATION_ROUTE"
        private const val LA_CANCEL_INVITE_ROUTE = "$LOCAL_AUTHORITY_ROUTE/$CANCEL_INVITE_ROUTE"
        private const val LA_CANCEL_INVITE_CONFIRMATION_ROUTE = "$LOCAL_AUTHORITY_ROUTE/$CANCEL_INVITE_CONFIRMATION_ROUTE"

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

        fun getDeleteUserConfirmationRoute(deletedUserId: Long): String =
            UriTemplate(DELETE_USER_CONFIRMATION_ROUTE).expand(deletedUserId).toASCIIString()

        fun getLaDeleteUserSuccessRoute(
            localAuthorityId: Int,
            deletedUserId: Long,
        ): String = UriTemplate(LA_DELETE_USER_CONFIRMATION_ROUTE).expand(localAuthorityId, deletedUserId).toASCIIString()

        fun getLaInviteUserSuccessRoute(localAuthorityId: Int): String =
            UriTemplate(LA_INVITE_NEW_USER_CONFIRMATION_ROUTE).expand(localAuthorityId).toASCIIString()

        fun getLaInviteNewUserRoute(localAuthorityId: Int): String =
            UriTemplate(LA_INVITE_NEW_USER_ROUTE).expand(localAuthorityId).toASCIIString()

        fun getLaCancelInviteRoute(
            localAuthorityId: Int,
            localAuthorityUserId: Long,
        ): String = UriTemplate(LA_CANCEL_INVITE_ROUTE).expand(localAuthorityId, localAuthorityUserId).toASCIIString()

        fun getCancelInviteConfirmationRoute(invitationId: Long): String =
            UriTemplate(CANCEL_INVITE_CONFIRMATION_ROUTE).expand(invitationId).toASCIIString()

        fun getLaCancelInviteSuccessRoute(
            localAuthorityId: Int,
            invitationId: Long,
        ): String = UriTemplate(LA_CANCEL_INVITE_CONFIRMATION_ROUTE).expand(localAuthorityId, invitationId).toASCIIString()
    }
}
