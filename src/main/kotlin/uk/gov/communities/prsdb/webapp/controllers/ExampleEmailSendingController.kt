package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.viewModels.LocalAuthorityInvitationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import java.security.Principal

// TODO PRSD-404: Remove this controller once there is another way to reach the EmailNotificationService
// This controller is an example controller to demo our integration with notify and should be removed once
// there is an integration that belongs to an intended releasable feature.
@PreAuthorize("hasRole('LA_ADMIN')")
@Controller
class ExampleEmailSendingController(
    var emailSender: EmailNotificationService<LocalAuthorityInvitationEmail>,
    var invitationService: LocalAuthorityInvitationService,
    var localAuthorityDataService: LocalAuthorityDataService,
) {
    @GetMapping("/send-test-email")
    fun exampleEmailPage(model: Model): String {
        model.addAttribute("contentHeader", "Send a test email using notify")
        model.addAttribute("title", "Send an email")
        return "sendTestEmail"
    }

    class Submission(
        val emailAddress: String,
    )

    @PostMapping("/send-test-email", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun sendEmail(
        model: Model,
        body: Submission,
        principal: Principal,
    ): String {
        try {
            val currentAuthority = localAuthorityDataService.getLocalAuthorityForUser(principal.name)!!
            val token = invitationService.createInvitationToken(body.emailAddress, currentAuthority)
            val invitationLinkAddress = invitationService.buildInvitationUri(token)
            emailSender.sendEmail(
                body.emailAddress,
                LocalAuthorityInvitationEmail(currentAuthority, invitationLinkAddress),
            )

            model.addAttribute("contentHeader", "You have sent a test email to ${body.emailAddress}")
            model.addAttribute("title", "Email sent")
            return "index"
        } catch (retryException: TransientEmailSentException) {
            model.addAttribute("contentHeader", "That didn't work. Please try again.")
            model.addAttribute("title", "Send an email")
            return "sendTestEmail"
        }
    }

    @GetMapping("/invitation")
    fun magicLink(
        model: Model,
        token: String,
    ): String {
        val authority = invitationService.getAuthorityForToken(token)
        model.addAttribute("contentHeader", "The local authority issuing that token was: ${authority.name}")
        model.addAttribute("title", "Magic link")
        return "index"
    }
}
