package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.viewmodel.ExampleEmail

// TODO PRSD-404: Remove this controller once there is another way to reach the EmailNotificationService
// This controller is an example controller to demo our integration with notify and should be removed once
// there is an integration that belongs to an intended releasable feature.
@Controller
class ExampleEmailSendingController(
    var emailSender: EmailNotificationService<ExampleEmail>,
) {
    @GetMapping("/send-test-email")
    fun exampleEmailPage(model: Model): String {
        model.addAttribute("contentHeader", "Send a test email using notify")
        model.addAttribute("title", "Send an email")
        model.addAttribute("serviceName", SERVICE_NAME)
        return "sendTestEmail"
    }

    class Submission(
        val emailAddress: String,
    )

    @PostMapping("/send-test-email", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun sendEmail(
        model: Model,
        body: Submission,
    ): String {
        model.addAttribute("serviceName", SERVICE_NAME)
        try {
            emailSender.sendEmail(body.emailAddress, ExampleEmail("Lucky Recipient"))
            model.addAttribute("contentHeader", "You have sent a test email to ${body.emailAddress}")
            model.addAttribute("title", "Email sent")
            return "index"
        } catch (retryException: TransientEmailSentException) {
            model.addAttribute("contentHeader", "That didn't work. Please try again.")
            model.addAttribute("title", "Send an email")
            return "sendTestEmail"
        }
    }
}
