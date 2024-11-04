package uk.gov.communities.prsdb.webapp.controllers

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.dataModels.ConfirmedEmailDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.LocalAuthorityInvitationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import java.security.Principal
import java.util.Locale

@PreAuthorize("hasRole('LA_ADMIN')")
@Controller
@RequestMapping("/manage-users")
class ManageLocalAuthorityUsersController(
    var emailSender: EmailNotificationService<LocalAuthorityInvitationEmail>,
    var invitationService: LocalAuthorityInvitationService,
    val localAuthorityDataService: LocalAuthorityDataService,
    @Qualifier("messageSource") private val messageSource: MessageSource,
) {
    @GetMapping
    fun index(
        model: Model,
        principal: Principal,
    ): String {
        val currentUserLocalAuthority = localAuthorityDataService.getLocalAuthorityForUser(principal.name)!!

        val activeUsers = localAuthorityDataService.getLocalAuthorityUsersForLocalAuthority(currentUserLocalAuthority)
        // TODO: Get these from LocalAuthorityUserInvitation with userName=email, isManager=false and isPending=true
        val pendingUsers =
            listOf(
                LocalAuthorityUserDataModel("Invited user 1", isManager = false, isPending = true),
                LocalAuthorityUserDataModel("Invited user 2", isManager = false, isPending = true),
            )

        val users = activeUsers + pendingUsers

        model.addAttribute(
            "contentHeader",
            messageSource.getMessage("manageLAUsers.contentHeader.part1", null, Locale("en")) +
                " " + currentUserLocalAuthority.name +
                messageSource.getMessage("manageLAUsers.contentHeader.part2", null, Locale("en")),
        )
        model.addAttribute("title", messageSource.getMessage("manageLAUsers.title", null, Locale("en")))
        model.addAttribute("serviceName", SERVICE_NAME)
        model.addAttribute("userList", users)
        model.addAttribute(
            "tableColumnHeadings",
            listOf(
                messageSource.getMessage("manageLAUsers.table.column1Heading", null, Locale("en")),
                messageSource.getMessage("manageLAUsers.table.column2Heading", null, Locale("en")),
                messageSource.getMessage("manageLAUsers.table.column3Heading", null, Locale("en")),
                "",
            ),
        )

        return "manageLAUsers"
    }

    @GetMapping("/invite-new-user")
    fun inviteNewUser(
        model: Model,
        principal: Principal,
    ): String {
        val currentAuthority = localAuthorityDataService.getLocalAuthorityForUser(principal.name)!!
        model.addAttribute("councilName", currentAuthority.name)
        model.addAttribute("serviceName", SERVICE_NAME)
        model.addAttribute("confirmedEmailDataModel", ConfirmedEmailDataModel())

        return "inviteLAUser"
    }

    @PostMapping("/invite-new-user", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun sendInvitation(
        model: Model,
        @Valid
        @ModelAttribute
        emailModel: ConfirmedEmailDataModel,
        bindingResult: BindingResult,
        principal: Principal,
        redirectAttributes: RedirectAttributes,
    ): String {
        model.addAttribute("serviceName", SERVICE_NAME)
        val currentAuthority = localAuthorityDataService.getLocalAuthorityForUser(principal.name)!!
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

            // TODO PRSD-404 Create a more permanent success template, rather than (ab)using "index" here
            redirectAttributes.addFlashAttribute("contentHeader", "You have sent a test email to ${emailModel.email}")
            redirectAttributes.addFlashAttribute("title", "Email sent")
            return "redirect:invite-new-user/success"
        } catch (retryException: TransientEmailSentException) {
            bindingResult.reject("addLAUser.error.retryable")
            return "inviteLAUser"
        }
    }

    @GetMapping("/invite-new-user/success")
    fun successInvitedNewUser() = "index"
}
