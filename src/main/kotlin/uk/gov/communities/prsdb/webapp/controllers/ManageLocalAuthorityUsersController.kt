package uk.gov.communities.prsdb.webapp.controllers

import jakarta.validation.Valid
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
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.dataModels.ConfirmedEmailDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserAccessLevelDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.LocalAuthorityInvitationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import java.security.Principal

@PreAuthorize("hasRole('LA_ADMIN')")
@Controller
@RequestMapping("/local-authority/{localAuthorityId}")
class ManageLocalAuthorityUsersController(
    var emailSender: EmailNotificationService<LocalAuthorityInvitationEmail>,
    var invitationService: LocalAuthorityInvitationService,
    val localAuthorityDataService: LocalAuthorityDataService,
) {
    @GetMapping("/manage-users")
    fun index(
        @PathVariable localAuthorityId: Int,
        model: Model,
        principal: Principal,
        @RequestParam(value = "page", required = false) page: Int = 1,
    ): String {
        val (currentUser, currentUserLocalAuthority) =
            localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(localAuthorityId, principal.name)

        val pagedUserList =
            localAuthorityDataService.getPaginatedUsersAndInvitations(
                currentUserLocalAuthority,
                page - 1,
            )

        if (pagedUserList.totalPages < page) {
            return "redirect:/local-authority/{localAuthorityId}/manage-users"
        }

        model.addAttribute("currentUser", currentUser)
        model.addAttribute("localAuthority", currentUserLocalAuthority)
        model.addAttribute("userList", pagedUserList)
        model.addAttribute("totalPages", pagedUserList.totalPages)
        model.addAttribute("currentPage", page)

        return "manageLAUsers"
    }

    @GetMapping("/edit-user/{localAuthorityUserId}")
    fun getEditUserAccessLevelPage(
        @PathVariable localAuthorityId: Int,
        @PathVariable localAuthorityUserId: Long,
        principal: Principal,
        model: Model,
    ): String {
        val (currentUser, _) =
            localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                localAuthorityId,
                principal.name,
            )
        if (currentUser.id == localAuthorityUserId) {
            throw AccessDeniedException("Local authority users cannot edit their own accounts; another admin must do so")
        }

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
        @ModelAttribute localAuthorityUserAccessLevel: LocalAuthorityUserAccessLevelDataModel,
        principal: Principal,
    ): String {
        val (currentUser, _) =
            localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                localAuthorityId,
                principal.name,
            )
        if (currentUser.id == localAuthorityUserId) {
            throw AccessDeniedException("Local authority users cannot edit their own accounts; another admin must do so")
        }
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
    ): String {
        val (currentUser, _) =
            localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                localAuthorityId,
                principal.name,
            )
        if (currentUser.id == localAuthorityUserId) {
            throw AccessDeniedException("Local authority users cannot delete their own accounts; another admin must do so")
        }
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
    ): String {
        val (currentUser, _) =
            localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                localAuthorityId,
                principal.name,
            )
        if (currentUser.id == localAuthorityUserId) {
            throw AccessDeniedException("Local authority users cannot delete their own accounts; another admin must do so")
        }
        val user = localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(localAuthorityUserId, localAuthorityId)

        localAuthorityDataService.deleteUser(localAuthorityUserId)

        redirectAttributes.addFlashAttribute("deletedUserName", user.userName)
        return "redirect:../delete-user/success"
    }

    @GetMapping("/delete-user/success")
    fun deleteUserSuccess(
        @PathVariable localAuthorityId: Int,
        model: Model,
        principal: Principal,
    ): String {
        val (_, authority) =
            localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                localAuthorityId,
                principal.name,
            )
        model.addAttribute("localAuthority", authority)
        return "deleteLAUserSuccess"
    }

    @GetMapping("/invite-new-user")
    fun inviteNewUser(
        @PathVariable localAuthorityId: Int,
        model: Model,
        principal: Principal,
    ): String {
        val (_, currentAuthority) =
            localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                localAuthorityId,
                principal.name,
            )
        model.addAttribute("councilName", currentAuthority.name)
        model.addAttribute("confirmedEmailDataModel", ConfirmedEmailDataModel())

        return "inviteLAUser"
    }

    @PostMapping("/invite-new-user", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun sendInvitation(
        @PathVariable localAuthorityId: Int,
        model: Model,
        @Valid
        @ModelAttribute
        emailModel: ConfirmedEmailDataModel,
        bindingResult: BindingResult,
        principal: Principal,
        redirectAttributes: RedirectAttributes,
    ): String {
        val (_, currentAuthority) =
            localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                localAuthorityId,
                principal.name,
            )
        model.addAttribute("councilName", currentAuthority.name)

        if (bindingResult.hasErrors()) {
            return "inviteLAUser"
        }

        try {
            val token = invitationService.createInvitationToken(emailModel.email, currentAuthority)
            val invitationLinkAddress = invitationService.buildInvitationUri(token)
            emailSender.sendEmail(
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
    ): String {
        val (_, currentAuthority) =
            localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                localAuthorityId,
                principal.name,
            )
        model.addAttribute("localAuthority", currentAuthority)
        return "inviteLAUserSuccess"
    }

    @GetMapping("/cancel-invitation/{invitationId}")
    fun confirmCancelInvitation(
        @PathVariable localAuthorityId: Int,
        @PathVariable invitationId: Long,
        principal: Principal,
        model: Model,
    ): String {
        val invitation = invitationService.getInvitationById(invitationId)

        val (_, authority) =
            localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                localAuthorityId,
                principal.name,
            )

        if (authority.id != invitation.invitingAuthority.id) {
            throw AccessDeniedException(
                "Local authority user for LA ${authority.name} tried to cancel an invitation from LA ${invitation.invitingAuthority.name}",
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

        invitationService.deleteInvitation(invitation)

        redirectAttributes.addFlashAttribute("deletedEmail", invitation.invitedEmail)
        redirectAttributes.addFlashAttribute("localAuthority", invitation.invitingAuthority)
        return "redirect:../cancel-invitation/success"
    }

    @GetMapping("/cancel-invitation/success")
    fun cancelInvitationSuccess(
        @PathVariable localAuthorityId: String,
        model: Model,
    ): String = "cancelLAUserInvitationSuccess"
}
