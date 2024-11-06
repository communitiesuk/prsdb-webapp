package uk.gov.communities.prsdb.webapp.controllers

import jakarta.validation.Valid
import org.springframework.http.MediaType
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
import uk.gov.communities.prsdb.webapp.models.dataModels.RadioButtonDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.LocalAuthorityInvitationEmail
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
        val currentUserLocalAuthority =
            localAuthorityDataService.getLocalAuthorityIfAuthorizedUser(localAuthorityId, principal.name)

        val pagedUserList =
            localAuthorityDataService.getPaginatedUsersAndInvitations(
                currentUserLocalAuthority,
                page - 1,
            )

        if (pagedUserList.totalPages < page) {
            return "redirect:/local-authority/{localAuthorityId}/manage-users"
        }

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
        localAuthorityDataService.getLocalAuthorityIfAuthorizedUser(localAuthorityId, principal.name)

        val localAuthorityUser =
            localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(localAuthorityUserId, localAuthorityId)

        model.addAttribute("localAuthorityUser", localAuthorityUser)
        model.addAttribute(
            "options",
            listOf(
                RadioButtonDataModel(
                    false,
                    "editLAUserAccess.radios.option.basic.label",
                    "editLAUserAccess.radios.option.basic.hint",
                ),
                RadioButtonDataModel(
                    true,
                    "editLAUserAccess.radios.option.admin.label",
                    "editLAUserAccess.radios.option.admin.hint",
                ),
            ),
        )

        return "editLAUserAccess"
    }

    @PostMapping("/edit-user/{localAuthorityUserId}")
    fun patchUserAccessLevel(
        @PathVariable localAuthorityId: Int,
        @PathVariable localAuthorityUserId: Long,
        @ModelAttribute localAuthorityUserAccessLevel: LocalAuthorityUserAccessLevelDataModel,
        principal: Principal,
    ): String {
        localAuthorityDataService.getLocalAuthorityIfAuthorizedUser(localAuthorityId, principal.name)
        localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(localAuthorityUserId, localAuthorityId)

        localAuthorityDataService.updateUserAccessLevel(localAuthorityUserAccessLevel, localAuthorityUserId)
        return "redirect:/local-authority/{localAuthorityId}/manage-users"
    }

    @GetMapping("/invite-new-user")
    fun inviteNewUser(
        @PathVariable localAuthorityId: Int,
        model: Model,
        principal: Principal,
    ): String {
        val currentAuthority = localAuthorityDataService.getLocalAuthorityIfAuthorizedUser(localAuthorityId, principal.name)
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
        val currentAuthority = localAuthorityDataService.getLocalAuthorityIfAuthorizedUser(localAuthorityId, principal.name)
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
        val currentAuthority = localAuthorityDataService.getLocalAuthorityIfAuthorizedUser(localAuthorityId, principal.name)
        model.addAttribute("localAuthority", currentAuthority)
        return "inviteLAUserSuccess"
    }
}
