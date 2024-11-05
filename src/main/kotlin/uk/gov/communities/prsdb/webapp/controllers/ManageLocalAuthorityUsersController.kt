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
@RequestMapping("/local-authority/{localAuthorityId}/manage-users")
class ManageLocalAuthorityUsersController(
    var emailSender: EmailNotificationService<LocalAuthorityInvitationEmail>,
    var invitationService: LocalAuthorityInvitationService,
    val localAuthorityDataService: LocalAuthorityDataService,
) {
    @GetMapping
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

        model.addAttribute("localAuthority", currentUserLocalAuthority.name)
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
        val localAuthorityUser =
            localAuthorityDataService.getLocalAuthorityUserIfAuthorizedUser(
                localAuthorityUserId,
                localAuthorityId,
                principal.name,
            )

        model.addAttribute("localAuthorityUser", localAuthorityUser)
        model.addAttribute(
            "options",
            listOf(
                RadioButtonDataModel(
                    false,
                    "editLAUserAccess.radios.option.one.label",
                    "editLAUserAccess.radios.option.one.hint",
                ),
                RadioButtonDataModel(
                    true,
                    "editLAUserAccess.radios.option.two.label",
                    "editLAUserAccess.radios.option.two.hint",
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
        localAuthorityDataService.updateUserAccessLevel(
            localAuthorityUserAccessLevel,
            localAuthorityUserId,
            localAuthorityId,
            principal.name,
        )
        return "redirect:/local-authority/{localAuthorityId}/manage-users"
    }

    @GetMapping("/invite-new-user")
    fun exampleEmailPage(model: Model): String {
        model.addAttribute("title", "sendEmail.send.title")
        model.addAttribute("contentHeader", "sendEmail.send.header")
        return "sendTestEmail"
    }

    @PostMapping("/invite-new-user", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun sendEmail(
        @PathVariable localAuthorityId: Int,
        model: Model,
        @Valid
        emailModel: ConfirmedEmailDataModel,
        result: BindingResult,
        principal: Principal,
    ): String {
        try {
            val emailAddress: String = emailModel.email
            val currentAuthority =
                localAuthorityDataService.getLocalAuthorityIfAuthorizedUser(localAuthorityId, principal.name)
            val token = invitationService.createInvitationToken(emailAddress, currentAuthority)
            val invitationLinkAddress = invitationService.buildInvitationUri(token)
            emailSender.sendEmail(
                emailAddress,
                LocalAuthorityInvitationEmail(currentAuthority, invitationLinkAddress),
            )

            model.addAttribute("title", "sendEmail.sent.title")
            model.addAttribute("contentHeader", "sendEmail.sent.header")
            model.addAttribute("contentHeaderParams", emailAddress)
            return "index"
        } catch (retryException: TransientEmailSentException) {
            model.addAttribute("title", "sendEmail.send.title")
            model.addAttribute("contentHeader", "sendEmail.send.errorTitle")
            return "sendTestEmail"
        }
    }
}
