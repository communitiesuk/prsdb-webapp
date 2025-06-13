package uk.gov.communities.prsdb.webapp.ephemeral

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.ExampleEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService

@Component
class ExampleEmailSendingApplicationRunner(
    private val emailSender: EmailNotificationService<ExampleEmail>,
    private val context: ApplicationContext,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        val recipientAddress = "address"
        val email = ExampleEmail(firstName = "John")
        emailSender.sendEmail(recipientAddress, email)

        SpringApplication.exit(context, { 0 }).also {
            println("Example email sent successfully. Application will exit now.")
        }
    }
}
