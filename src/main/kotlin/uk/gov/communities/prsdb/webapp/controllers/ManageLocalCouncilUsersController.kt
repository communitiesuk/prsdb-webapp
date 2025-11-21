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
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilDashboardController.Companion.LOCAL_COUNCIL_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController.Companion.LOCAL_COUNCIL_ROUTE
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

@PreAuthorize("hasAnyRole('LOCAL_COUNCIL_ADMIN', 'SYSTEM_OPERATOR')")
@PrsdbController
@RequestMapping(LOCAL_COUNCIL_ROUTE)
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
        @PathVariable localCouncilId: Int,
        model: Model,
        principal: Principal,
        @RequestParam(value = "page", required = false) @Min(1) page: Int = 1,
        request: HttpServletRequest,
    ): String {
        val loggedInLocalCouncilAdmin =
            getCurrentUserIfTheyAreAnLocalCouncilAdminForTheCurrentLocalCouncil(principal, localCouncilId, request)

        val localCouncil = getLocalCouncil(principal, localCouncilId, request)

        val pagedUserList =
            localCouncilDataService.getPaginatedUsersAndInvitations(
                localCouncil,
                page - 1,
                filterOutLocalCouncilAdminInvitations = !request.isUserInRole(ROLE_SYSTEM_OPERATOR),
            )

        if (pagedUserList.totalPages != 0 && pagedUserList.totalPages < page) {
            return "redirect:${getLocalCouncilManageUsersRoute(localCouncilId)}"
        }

        model.addAttribute("currentUserId", loggedInLocalCouncilAdmin?.id)
        model.addAttribute("localCouncil", localCouncil)
        model.addAttribute("userList", pagedUserList)
        model.addAttribute(
            "paginationViewModel",
            PaginationViewModel(page, pagedUserList.totalPages, request),
        )
        model.addAttribute("userCanEditTheirOwnAccount", request.isUserInRole(ROLE_SYSTEM_OPERATOR))

        // TODO: PRSD-672 - if the user is not an la admin, make this a link to the system operator dashboard
        model.addAttribute("dashboardUrl", LOCAL_COUNCIL_DASHBOARD_URL)

        return "manageLocalCouncilUsers"
    }

    @GetMapping("/$EDIT_USER_ROUTE")
    fun getEditUserAccessLevelPage(
        @PathVariable localCouncilId: Int,
        @PathVariable localCouncilUserId: Long,
        principal: Principal,
        model: Model,
        request: HttpServletRequest,
    ): String {
        throwErrorIfNonSystemOperatorIsUpdatingTheirOwnAccount(principal, localCouncilId, localCouncilUserId, request)

        val localCouncilUser =
            localCouncilDataService.getLocalCouncilUserIfAuthorizedLocalCouncil(localCouncilUserId, localCouncilId)

        model.addAttribute("backUrl", "../$MANAGE_USERS_PATH_SEGMENT")
        model.addAttribute("localCouncilUser", localCouncilUser)
        model.addAttribute(
            "options",
            listOf(
                RadiosButtonViewModel(
                    false,
                    "basic",
                    "editLocalCouncilUserAccess.radios.option.basic.label",
                    "editLocalCouncilUserAccess.radios.option.basic.hint",
                ),
                RadiosButtonViewModel(
                    true,
                    "admin",
                    "editLocalCouncilUserAccess.radios.option.admin.label",
                    "editLocalCouncilUserAccess.radios.option.admin.hint",
                ),
            ),
        )
        model.addAttribute("deleteUserUrl", getLocalCouncilDeleteUserRoute(localCouncilId, localCouncilUserId))

        return "editLocalCouncilUserAccess"
    }

    @PostMapping("/$EDIT_USER_ROUTE")
    fun updateUserAccessLevel(
        @PathVariable localCouncilId: Int,
        @PathVariable localCouncilUserId: Long,
        @ModelAttribute localCouncilUserAccessLevel: LocalCouncilUserAccessLevelRequestModel,
        principal: Principal,
        request: HttpServletRequest,
    ): String {
        throwErrorIfNonSystemOperatorIsUpdatingTheirOwnAccount(principal, localCouncilId, localCouncilUserId, request)

        localCouncilDataService.getLocalCouncilUserIfAuthorizedLocalCouncil(localCouncilUserId, localCouncilId)

        localCouncilDataService.updateUserAccessLevel(localCouncilUserAccessLevel, localCouncilUserId)
        return "redirect:${getLocalCouncilManageUsersRoute(localCouncilId)}"
    }

    @GetMapping("/$DELETE_USER_ROUTE")
    fun confirmDeleteUser(
        @PathVariable localCouncilId: Int,
        @PathVariable deleteeId: Long,
        model: Model,
        principal: Principal,
        request: HttpServletRequest,
    ): String {
        throwErrorIfNonSystemOperatorIsUpdatingTheirOwnAccount(principal, localCouncilId, deleteeId, request)

        val userToDelete =
            localCouncilDataService.getLocalCouncilUserIfAuthorizedLocalCouncil(deleteeId, localCouncilId)
        model.addAttribute("user", userToDelete)
        model.addAttribute("backLinkPath", "../$EDIT_USER_PATH_SEGMENT/$deleteeId")
        return "deleteLocalCouncilUser"
    }

    @PostMapping("/$DELETE_USER_ROUTE")
    fun deleteUser(
        @PathVariable localCouncilId: Int,
        @PathVariable deleteeId: Long,
        principal: Principal,
        redirectAttributes: RedirectAttributes,
        request: HttpServletRequest,
    ): String {
        throwErrorIfNonSystemOperatorIsUpdatingTheirOwnAccount(principal, localCouncilId, deleteeId, request)
        val userBeingDeleted = localCouncilDataService.getLocalCouncilUserIfAuthorizedLocalCouncil(deleteeId, localCouncilId)

        localCouncilDataService.deleteUser(userBeingDeleted)

        if (request.isUserInRole(ROLE_SYSTEM_OPERATOR) &&
            (request.isUserInRole(ROLE_LOCAL_COUNCIL_ADMIN) || request.isUserInRole(ROLE_LOCAL_COUNCIL_USER))
        ) {
            // If the user is a system operator they can delete themself from the local_authority_user table
            // If this happens we will need to update their user roles as the Manage LA Users page
            // will throw an error if they have the LA_ADMIN role but are no longer in the local_authority_users table.
            val currentUser = localCouncilDataService.getLocalCouncilUser(principal.name)
            if (currentUser.id == userBeingDeleted.id) {
                securityContextService.refreshContext()
            }
        }

        localCouncilDataService.addDeletedUserToSession(userBeingDeleted)
        return "redirect:../$DELETE_USER_CONFIRMATION_ROUTE"
    }

    @GetMapping("/$DELETE_USER_CONFIRMATION_ROUTE")
    fun deleteUserSuccess(
        @PathVariable localCouncilId: Int,
        @PathVariable deleteeId: Long,
        model: Model,
        principal: Principal,
        request: HttpServletRequest,
    ): String {
        val userDeletedThisSession = localCouncilDataService.getUserDeletedThisSessionById(deleteeId)

        model.addAttribute("deletedUserName", userDeletedThisSession.name)

        model.addAttribute("localCouncil", getLocalCouncil(principal, localCouncilId, request))

        model.addAttribute("returnToManageUsersUrl", getLocalCouncilManageUsersRoute(localCouncilId))

        return "deleteLocalCouncilUserSuccess"
    }

    @GetMapping("/$INVITE_NEW_USER_PATH_SEGMENT")
    fun inviteNewUser(
        @PathVariable localCouncilId: Int,
        model: Model,
        principal: Principal,
        request: HttpServletRequest,
    ): String {
        val councilName = getLocalCouncil(principal, localCouncilId, request).name
        val councilNameBeginsWithVowel = councilName[0].uppercase() in VOWELS

        model.addAttribute("councilName", councilName)
        model.addAttribute("councilNameBeginsWithVowel", councilNameBeginsWithVowel)
        model.addAttribute("confirmedEmailRequestModel", ConfirmedEmailRequestModel())

        return "inviteLocalCouncilUser"
    }

    @PostMapping("/$INVITE_NEW_USER_PATH_SEGMENT", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun sendInvitation(
        @PathVariable localCouncilId: Int,
        model: Model,
        @Valid
        @ModelAttribute
        emailModel: ConfirmedEmailRequestModel,
        bindingResult: BindingResult,
        principal: Principal,
        redirectAttributes: RedirectAttributes,
        request: HttpServletRequest,
    ): String {
        val currentCouncil = getLocalCouncil(principal, localCouncilId, request)
        model.addAttribute("councilName", currentCouncil.name)

        if (bindingResult.hasErrors()) {
            return "inviteLocalCouncilUser"
        }

        try {
            val token = invitationService.createInvitationToken(emailModel.email, currentCouncil)
            val invitationLinkAddress = absoluteUrlProvider.buildInvitationUri(token)
            invitationEmailSender.sendEmail(
                emailModel.email,
                LocalCouncilInvitationEmail(
                    currentCouncil,
                    invitationLinkAddress,
                    absoluteUrlProvider.buildLocalCouncilDashboardUri().toString(),
                ),
            )
            localCouncilDataService.sendUserInvitedEmailsToAdmins(
                currentCouncil,
                emailModel.email,
            )

            localCouncilDataService.addInvitedLocalCouncilUserToSession(localCouncilId, emailModel.email)

            return "redirect:$INVITE_USER_CONFIRMATION_ROUTE"
        } catch (retryException: TransientEmailSentException) {
            bindingResult.reject("addLAUser.error.retryable")
            return "inviteLocalCouncilUser"
        }
    }

    @GetMapping("/$INVITE_USER_CONFIRMATION_ROUTE")
    fun inviteNewUserConfirmation(
        @PathVariable localCouncilId: Int,
        principal: Principal,
        model: Model,
        request: HttpServletRequest,
    ): String {
        val invitedEmail =
            localCouncilDataService.getLastLocalCouncilUserInvitedThisSession(localCouncilId)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No email address found in the session for a user invited to local council with id $localCouncilId",
                )

        model.addAttribute("localCouncil", getLocalCouncil(principal, localCouncilId, request))
        model.addAttribute("dashboardUrl", LOCAL_COUNCIL_DASHBOARD_URL)
        model.addAttribute("invitedEmailAddress", invitedEmail)
        return "inviteLocalCouncilUserSuccess"
    }

    @GetMapping("/$CANCEL_INVITE_ROUTE")
    fun confirmCancelInvitation(
        @PathVariable localCouncilId: Int,
        @PathVariable invitationId: Long,
        principal: Principal,
        model: Model,
        request: HttpServletRequest,
    ): String {
        val invitation =
            invitationService.getInvitationByIdOrNull(invitationId) ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Invitation with id $invitationId was not found in the local_council_invitations table",
            )

        val council = getLocalCouncil(principal, localCouncilId, request)

        if (council.id != invitation.invitingCouncil.id) {
            throw AccessDeniedException(
                "A user on the Manage Local Council Users page for ${council.name} tried to cancel an invitation " +
                    "from Local Council ${invitation.invitingCouncil.name}",
            )
        }

        model.addAttribute("backLinkPath", "../$MANAGE_USERS_PATH_SEGMENT")
        model.addAttribute("email", invitation.invitedEmail)

        return "cancelLocalCouncilUserInvitation"
    }

    @PostMapping("/$CANCEL_INVITE_ROUTE")
    fun cancelInvitation(
        @PathVariable localCouncilId: Int,
        @PathVariable invitationId: Long,
        redirectAttributes: RedirectAttributes,
    ): String {
        val invitation =
            invitationService.getInvitationByIdOrNull(invitationId) ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Invitation with id $invitationId was not found in the local_council_invitations table",
            )

        invitationService.deleteInvitation(invitationId)

        cancellationEmailSender.sendEmail(
            invitation.invitedEmail,
            LocalCouncilInvitationCancellationEmail(invitation.invitingCouncil),
        )

        localCouncilDataService.addCancelledInvitationToSession(
            invitation,
        )

        return "redirect:../$CANCEL_INVITE_CONFIRMATION_ROUTE"
    }

    @GetMapping("/$CANCEL_INVITE_CONFIRMATION_ROUTE")
    fun cancelInvitationSuccess(
        @PathVariable localCouncilId: Int,
        @PathVariable invitationId: Long,
        model: Model,
        principal: Principal,
        request: HttpServletRequest,
    ): String {
        val invitationDeletedThisSession = localCouncilDataService.getInvitationCancelledThisSessionById(invitationId)

        model.addAttribute("deletedEmail", invitationDeletedThisSession.invitedEmail)

        model.addAttribute("localCouncil", getLocalCouncil(principal, localCouncilId, request))
        model.addAttribute("returnToManageUsersUrl", getLocalCouncilManageUsersRoute(localCouncilId))

        return "cancelLocalCouncilUserInvitationSuccess"
    }

    private fun throwErrorIfNonSystemOperatorIsUpdatingTheirOwnAccount(
        principal: Principal,
        localCouncilId: Int,
        localCouncilUserId: Long,
        request: HttpServletRequest,
    ) {
        if (!request.isUserInRole(ROLE_SYSTEM_OPERATOR)) {
            val (currentUser, _) =
                localCouncilDataService.getUserAndLocalCouncilIfAuthorizedUser(
                    localCouncilId,
                    principal.name,
                )
            if (currentUser.id == localCouncilUserId) {
                throw AccessDeniedException("Local council users cannot edit their own accounts; another admin must do so")
            }
        }
    }

    private fun getLocalCouncil(
        principal: Principal,
        localCouncilId: Int,
        request: HttpServletRequest,
    ): LocalCouncil =
        if (request.isUserInRole(ROLE_SYSTEM_OPERATOR)) {
            localCouncilService.retrieveLocalCouncilById(localCouncilId)
        } else {
            val localCouncilUserAndlocalCouncil =
                localCouncilDataService.getUserAndLocalCouncilIfAuthorizedUser(
                    localCouncilId,
                    principal.name,
                )
            localCouncilUserAndlocalCouncil.second
        }

    private fun getCurrentUserIfTheyAreAnLocalCouncilAdminForTheCurrentLocalCouncil(
        principal: Principal,
        localCouncilId: Int,
        request: HttpServletRequest,
    ): LocalCouncilUserDataModel? {
        if (!request.isUserInRole(ROLE_LOCAL_COUNCIL_ADMIN)) {
            return null
        }
        try {
            val (currentUser, _) =
                localCouncilDataService.getUserAndLocalCouncilIfAuthorizedUser(
                    localCouncilId,
                    principal.name,
                )
            return currentUser
        } catch (exception: AccessDeniedException) {
            // This is expected if the user is not an admin for the current LA
            return null
        }
    }

    companion object {
        const val LOCAL_COUNCIL_ROUTE = "/$LOCAL_COUNCIL_PATH_SEGMENT/{localCouncilId}"
        const val EDIT_USER_ROUTE = "$EDIT_USER_PATH_SEGMENT/{localCouncilUserId}"
        const val DELETE_USER_ROUTE = "$DELETE_USER_PATH_SEGMENT/{deleteeId}"
        const val DELETE_USER_CONFIRMATION_ROUTE = "$DELETE_USER_ROUTE/$CONFIRMATION_PATH_SEGMENT"
        const val INVITE_USER_CONFIRMATION_ROUTE = "$INVITE_NEW_USER_PATH_SEGMENT/$CONFIRMATION_PATH_SEGMENT"
        const val CANCEL_INVITE_ROUTE = "$CANCEL_INVITATION_PATH_SEGMENT/{invitationId}"
        const val CANCEL_INVITE_CONFIRMATION_ROUTE = "$CANCEL_INVITE_ROUTE/$CONFIRMATION_PATH_SEGMENT"

        private const val LOCAL_COUNCIL_MANAGE_USERS_ROUTE = "$LOCAL_COUNCIL_ROUTE/$MANAGE_USERS_PATH_SEGMENT"
        private const val LOCAL_COUNCIL_EDIT_USER_ROUTE = "$LOCAL_COUNCIL_ROUTE/$EDIT_USER_ROUTE"
        private const val LOCAL_COUNCIL_DELETE_USER_ROUTE = "$LOCAL_COUNCIL_ROUTE/$DELETE_USER_ROUTE"
        private const val LOCAL_COUNCIL_DELETE_USER_CONFIRMATION_ROUTE = "$LOCAL_COUNCIL_ROUTE/$DELETE_USER_CONFIRMATION_ROUTE"
        private const val LOCAL_COUNCIL_INVITE_NEW_USER_ROUTE = "$LOCAL_COUNCIL_ROUTE/$INVITE_NEW_USER_PATH_SEGMENT"
        private const val LOCAL_COUNCIL_INVITE_NEW_USER_CONFIRMATION_ROUTE = "$LOCAL_COUNCIL_ROUTE/$INVITE_USER_CONFIRMATION_ROUTE"
        private const val LOCAL_COUNCIL_CANCEL_INVITE_ROUTE = "$LOCAL_COUNCIL_ROUTE/$CANCEL_INVITE_ROUTE"
        private const val LOCAL_COUNCIL_CANCEL_INVITE_CONFIRMATION_ROUTE = "$LOCAL_COUNCIL_ROUTE/$CANCEL_INVITE_CONFIRMATION_ROUTE"

        fun getLocalCouncilManageUsersRoute(localCouncilId: Int): String =
            UriTemplate(LOCAL_COUNCIL_MANAGE_USERS_ROUTE).expand(localCouncilId).toASCIIString()

        fun getLocalCouncilEditUserRoute(
            localCouncilId: Int,
            localCouncilUserId: Long,
        ): String = UriTemplate(LOCAL_COUNCIL_EDIT_USER_ROUTE).expand(localCouncilId, localCouncilUserId).toASCIIString()

        fun getLocalCouncilDeleteUserRoute(
            localCouncilId: Int,
            localCouncilUserId: Long,
        ): String = UriTemplate(LOCAL_COUNCIL_DELETE_USER_ROUTE).expand(localCouncilId, localCouncilUserId).toASCIIString()

        fun getDeleteUserConfirmationRoute(deletedUserId: Long): String =
            UriTemplate(DELETE_USER_CONFIRMATION_ROUTE).expand(deletedUserId).toASCIIString()

        fun getLocalCouncilDeleteUserSuccessRoute(
            localCouncilId: Int,
            deletedUserId: Long,
        ): String = UriTemplate(LOCAL_COUNCIL_DELETE_USER_CONFIRMATION_ROUTE).expand(localCouncilId, deletedUserId).toASCIIString()

        fun getLocalCouncilInviteUserSuccessRoute(localCouncilId: Int): String =
            UriTemplate(LOCAL_COUNCIL_INVITE_NEW_USER_CONFIRMATION_ROUTE).expand(localCouncilId).toASCIIString()

        fun getLocalCouncilInviteNewUserRoute(localCouncilId: Int): String =
            UriTemplate(LOCAL_COUNCIL_INVITE_NEW_USER_ROUTE).expand(localCouncilId).toASCIIString()

        fun getLocalCouncilCancelInviteRoute(
            localCouncilId: Int,
            localCouncilUserId: Long,
        ): String = UriTemplate(LOCAL_COUNCIL_CANCEL_INVITE_ROUTE).expand(localCouncilId, localCouncilUserId).toASCIIString()

        fun getCancelInviteConfirmationRoute(invitationId: Long): String =
            UriTemplate(CANCEL_INVITE_CONFIRMATION_ROUTE).expand(invitationId).toASCIIString()

        fun getLocalCouncilCancelInviteSuccessRoute(
            localCouncilId: Int,
            invitationId: Long,
        ): String = UriTemplate(LOCAL_COUNCIL_CANCEL_INVITE_CONFIRMATION_ROUTE).expand(localCouncilId, invitationId).toASCIIString()
    }
}
