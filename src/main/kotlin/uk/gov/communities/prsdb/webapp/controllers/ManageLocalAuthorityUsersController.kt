package uk.gov.communities.prsdb.webapp.controllers

import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.dataModels.ConfirmedEmailDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.LocalAuthorityInvitationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import java.security.Principal

@PreAuthorize("hasRole('LA_ADMIN')")
@Controller
@RequestMapping("/manage-users")
class ManageLocalAuthorityUsersController(
    var emailSender: EmailNotificationService<LocalAuthorityInvitationEmail>,
    var invitationService: LocalAuthorityInvitationService,
    val localAuthorityDataService: LocalAuthorityDataService,
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

        model.addAttribute("localAuthority", currentUserLocalAuthority.name)
        model.addAttribute("userList", users)
        model.addAttribute(
            "tableColumnHeadings",
            listOf(
                "manageLAUsers.table.column1Heading",
                "manageLAUsers.table.column2Heading",
                "manageLAUsers.table.column3Heading",
                "",
            ),
        )

        return "manageLAUsers"
    }

    @GetMapping("/invite-new-user")
    fun exampleEmailPage(model: Model): String {
        model.addAttribute("contentHeader", "Send a test email using notify")
        model.addAttribute("title", "Send an email")
        model.addAttribute("serviceName", SERVICE_NAME)
        return "sendTestEmail"
    }

    @PostMapping("/invite-new-user", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun sendEmail(
        model: Model,
        @Valid
        emailModel: ConfirmedEmailDataModel,
        result: BindingResult,
        principal: Principal,
    ): String {
        model.addAttribute("serviceName", SERVICE_NAME)
        try {
            result.allErrors.forEach { println(it.toString()) }
            val emailAddress: String = emailModel.email
            val currentAuthority = localAuthorityDataService.getLocalAuthorityForUser(principal.name)!!
            val token = invitationService.createInvitationToken(emailAddress, currentAuthority)
            val invitationLinkAddress = invitationService.buildInvitationUri(token)
            emailSender.sendEmail(
                emailAddress,
                LocalAuthorityInvitationEmail(currentAuthority, invitationLinkAddress),
            )

            model.addAttribute("contentHeader", "You have sent a test email to $emailAddress")
            model.addAttribute("title", "Email sent")
            return "index"
        } catch (retryException: TransientEmailSentException) {
            model.addAttribute("contentHeader", "That didn't work. Please try again.")
            model.addAttribute("title", "Send an email")
            return "sendTestEmail"
        }
    }
}
