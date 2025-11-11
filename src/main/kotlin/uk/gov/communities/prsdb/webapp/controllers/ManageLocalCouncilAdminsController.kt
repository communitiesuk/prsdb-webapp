package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CANCEL_INVITATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DELETE_ADMIN_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.EDIT_ADMIN_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.INVITE_LA_ADMIN_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MANAGE_LA_ADMINS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilAdminsController.Companion.SYSTEM_OPERATOR_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilUser
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.requestModels.InviteLocalCouncilAdminModel
import uk.gov.communities.prsdb.webapp.models.requestModels.LocalCouncilUserAccessLevelRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.PaginationViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilAdminInvitationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilInvitationCancellationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.SelectViewModel
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilDataService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import java.security.Principal

@PreAuthorize("hasRole('SYSTEM_OPERATOR')")
@PrsdbController
@RequestMapping(SYSTEM_OPERATOR_ROUTE)
class ManageLocalCouncilAdminsController(
    private val localCouncilService: LocalCouncilService,
    private val localCouncilDataService: LocalCouncilDataService,
    private val invitationEmailSender: EmailNotificationService<LocalCouncilAdminInvitationEmail>,
    private val cancellationEmailSender: EmailNotificationService<LocalCouncilInvitationCancellationEmail>,
    private val invitationService: LocalCouncilInvitationService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val securityContextService: SecurityContextService,
) {
    @GetMapping("/$INVITE_LA_ADMIN_PATH_SEGMENT")
    fun inviteLocalAuthorityAdmin(model: Model): String {
        addSelectOptionsToModel(model)
        model.addAttribute("inviteLocalAuthorityAdminModel", InviteLocalCouncilAdminModel())

        return "inviteLocalCouncilAdminUser"
    }

    @PostMapping("/$INVITE_LA_ADMIN_PATH_SEGMENT", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun sendInvitation(
        model: Model,
        @Valid
        @ModelAttribute
        inviteLocalCouncilAdminModel: InviteLocalCouncilAdminModel,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            addSelectOptionsToModel(model)
            return "inviteLocalCouncilAdminUser"
        }

        try {
            val localAuthority = localCouncilService.retrieveLocalAuthorityById(inviteLocalCouncilAdminModel.localAuthorityId!!)

            val token =
                invitationService.createInvitationToken(
                    inviteLocalCouncilAdminModel.email,
                    localAuthority,
                    invitedAsAdmin = true,
                )
            val invitationLinkAddress = absoluteUrlProvider.buildInvitationUri(token)
            invitationEmailSender.sendEmail(
                inviteLocalCouncilAdminModel.email,
                LocalCouncilAdminInvitationEmail(localAuthority, invitationLinkAddress),
            )

            redirectAttributes.addFlashAttribute("invitedEmailAddress", inviteLocalCouncilAdminModel.email)
            redirectAttributes.addFlashAttribute("localAuthorityName", localAuthority.name)
            return "redirect:$INVITE_LA_ADMIN_CONFIRMATION_ROUTE"
        } catch (retryException: TransientEmailSentException) {
            bindingResult.reject("addLAUser.error.retryable")
            return "inviteLocalCouncilAdminUser"
        }
    }

    private fun addSelectOptionsToModel(model: Model) {
        val localAuthoritiesSelectOptions =
            localCouncilService.retrieveAllLocalAuthorities().map {
                SelectViewModel(
                    value = it.id,
                    label = it.name,
                )
            }
        model.addAttribute("selectOptions", localAuthoritiesSelectOptions)
    }

    @GetMapping("/$INVITE_LA_ADMIN_PATH_SEGMENT/$CONFIRMATION_PATH_SEGMENT")
    fun confirmation(model: Model): String {
        if (model.getAttribute("invitedEmailAddress") == null || model.getAttribute("localAuthorityName") == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing attributes, has the user navigated directly to this page?")
        }

        model.addAttribute("inviteAnotherUserUrl", INVITE_LA_ADMIN_ROUTE)
        // TODO PRSD-672: Add link to the system operator dashboard
        model.addAttribute("dashboardUrl", "#")

        return "inviteLocalCouncilAdminConfirmation"
    }

    @GetMapping("/$MANAGE_LA_ADMINS_PATH_SEGMENT")
    fun manageAdmins(
        model: Model,
        principal: Principal,
        @RequestParam(value = "page", required = false) @Min(1) page: Int = 1,
        request: HttpServletRequest,
    ): String {
        val pagedUserList =
            localCouncilDataService.getPaginatedAdminUsersAndInvitations(page - 1)

        if (pagedUserList.totalPages != 0 && pagedUserList.totalPages < page) {
            return "redirect:$MANAGE_LA_ADMINS_ROUTE"
        }

        model.addAttribute("userList", pagedUserList)
        model.addAttribute(
            "paginationViewModel",
            PaginationViewModel(page, pagedUserList.totalPages, request),
        )
        model.addAttribute("cancelInvitationPathSegment", CANCEL_INVITATION_PATH_SEGMENT)
        model.addAttribute("editUserPathSegment", EDIT_ADMIN_PATH_SEGMENT)
        model.addAttribute("inviteAdminsUrl", INVITE_LA_ADMIN_ROUTE)
        return "manageLocalCouncilAdmins"
    }

    @GetMapping("/$EDIT_ADMIN_PATH_SEGMENT/{localAuthorityUserId}")
    fun editAdminsAccessLevel(
        @PathVariable localAuthorityUserId: Long,
        model: Model,
    ): String {
        val localAuthorityUser = localCouncilDataService.getLocalAuthorityUserById(localAuthorityUserId)
        model.addAttribute("backUrl", "../$MANAGE_LA_ADMINS_PATH_SEGMENT")
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
        model.addAttribute("deleteUserUrl", "$SYSTEM_OPERATOR_ROUTE/$DELETE_ADMIN_PATH_SEGMENT/$localAuthorityUserId")
        return "editLocalCouncilUserAccess"
    }

    @PostMapping("/$EDIT_ADMIN_PATH_SEGMENT/{localAuthorityUserId}")
    fun editAdminsAccessLevel(
        @PathVariable localAuthorityUserId: Long,
        @ModelAttribute localAuthorityUserAccessLevel: LocalCouncilUserAccessLevelRequestModel,
    ): String {
        localCouncilDataService.updateUserAccessLevel(localAuthorityUserAccessLevel, localAuthorityUserId)

        return "redirect:$MANAGE_LA_ADMINS_ROUTE"
    }

    @GetMapping("/$DELETE_ADMIN_PATH_SEGMENT/{localAuthorityUserId}")
    fun deleteAdmin(
        @PathVariable localAuthorityUserId: Long,
        model: Model,
    ): String {
        val localAuthorityUser = localCouncilDataService.getLocalAuthorityUserById(localAuthorityUserId)
        model.addAttribute("user", localAuthorityUser)
        model.addAttribute("backLinkPath", "../$EDIT_ADMIN_PATH_SEGMENT/$localAuthorityUserId")
        return "deleteLocalCouncilUser"
    }

    @PostMapping("/$DELETE_ADMIN_PATH_SEGMENT/{localAuthorityUserId}")
    fun deleteAdmin(
        @PathVariable localAuthorityUserId: Long,
        principal: Principal,
    ): String {
        val userBeingDeleted = localCouncilDataService.getLocalAuthorityUserById(localAuthorityUserId)

        // If the user is deleting their own admin account we will need to update their user roles
        val refreshSecurityContextAfterDelete = getIsCurrentUserBeingDeletedAsAdmin(principal, userBeingDeleted)

        localCouncilDataService.deleteUser(userBeingDeleted)

        if (refreshSecurityContextAfterDelete) {
            securityContextService.refreshContext()
        }

        localCouncilDataService.addDeletedUserToSession(userBeingDeleted)

        return "redirect:../$DELETE_ADMIN_PATH_SEGMENT/$localAuthorityUserId/$CONFIRMATION_PATH_SEGMENT"
    }

    @GetMapping("/$DELETE_ADMIN_PATH_SEGMENT/{localAuthorityUserId}/$CONFIRMATION_PATH_SEGMENT")
    fun deleteAdminConfirmation(
        @PathVariable localAuthorityUserId: Long,
        model: Model,
    ): String {
        val userDeletedThisSession = localCouncilDataService.getUserDeletedThisSessionById(localAuthorityUserId)

        model.addAttribute("deletedUserName", userDeletedThisSession.name)

        model.addAttribute("localAuthority", userDeletedThisSession.localCouncil)

        model.addAttribute("returnToManageUsersUrl", MANAGE_LA_ADMINS_ROUTE)

        return "deleteLocalCouncilUserSuccess"
    }

    @GetMapping("/$CANCEL_INVITATION_PATH_SEGMENT/{invitationId}")
    fun cancelAdminInvitation(
        @PathVariable invitationId: Long,
        model: Model,
    ): String {
        val invitation =
            invitationService.getAdminInvitationByIdOrNull(invitationId) ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Invitation with id $invitationId was not found in the local_authority_invitations table",
            )
        model.addAttribute("backLinkPath", "../$MANAGE_LA_ADMINS_PATH_SEGMENT")
        model.addAttribute("email", invitation.invitedEmail)

        return "cancelLocalCouncilUserInvitation"
    }

    @PostMapping("/$CANCEL_INVITATION_PATH_SEGMENT/{invitationId}")
    fun cancelAdminInvitation(
        @PathVariable invitationId: Long,
    ): String {
        val invitation =
            invitationService.getAdminInvitationByIdOrNull(invitationId) ?: throw ResponseStatusException(
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

        return "redirect:../$CANCEL_INVITATION_PATH_SEGMENT/$invitationId/$CONFIRMATION_PATH_SEGMENT"
    }

    @GetMapping("/$CANCEL_INVITATION_PATH_SEGMENT/{invitationId}/$CONFIRMATION_PATH_SEGMENT")
    fun cancelAdminInvitationConfirmation(
        @PathVariable invitationId: Long,
        model: Model,
    ): String {
        val invitationDeletedThisSession = localCouncilDataService.getInvitationCancelledThisSessionById(invitationId)

        model.addAttribute("deletedEmail", invitationDeletedThisSession.invitedEmail)
        model.addAttribute("localAuthority", invitationDeletedThisSession.invitingAuthority)
        model.addAttribute("returnToManageUsersUrl", MANAGE_LA_ADMINS_ROUTE)
        return "cancelLocalCouncilUserInvitationSuccess"
    }

    private fun getIsCurrentUserBeingDeletedAsAdmin(
        principal: Principal,
        userBeingDeleted: LocalCouncilUser,
    ): Boolean = principal.name == userBeingDeleted.baseUser.id

    companion object {
        const val SYSTEM_OPERATOR_ROUTE = "/$LOCAL_AUTHORITY_PATH_SEGMENT/$SYSTEM_OPERATOR_PATH_SEGMENT"
        const val INVITE_LA_ADMIN_ROUTE = "$SYSTEM_OPERATOR_ROUTE/$INVITE_LA_ADMIN_PATH_SEGMENT"
        const val MANAGE_LA_ADMINS_ROUTE = "$SYSTEM_OPERATOR_ROUTE/$MANAGE_LA_ADMINS_PATH_SEGMENT"

        const val INVITE_LA_ADMIN_CONFIRMATION_ROUTE = "$INVITE_LA_ADMIN_ROUTE/$CONFIRMATION_PATH_SEGMENT"
    }
}
