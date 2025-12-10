package uk.gov.communities.prsdb.webapp.application

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.IncompletePropertyReminderEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import kotlin.system.exitProcess

@Component
@Profile("web-server-deactivated & scheduled-task & incomplete-property-reminder-scheduled-task")
@Order(1)
class IncompletePropertiesReminderTaskApplicationRunner(
    private val context: ApplicationContext,
    private val emailSender: EmailNotificationService<IncompletePropertyReminderEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        println("Executing incomplete properties reminder scheduled task")

        // TODO - PRDS-1030 - retrieve incomplete properties older than 21 days

        sendEmailToLandlord("jasmin.conterio@softwire.com", "HARDCODED ADDRESS")

        val code =
            SpringApplication.exit(context, { 0 }).also {
                println("Scheduled task executed. Application will exit now.")
            }
        exitProcess(code)
    }

    private fun sendEmailToLandlord(
        emailAddress: String,
        propertyAddress: String,
    ) {
        val prsdUrl = absoluteUrlProvider.buildLandlordDashboardUri().toString()

        emailSender.sendEmail(
            emailAddress,
            IncompletePropertyReminderEmail(
                singleLineAddress = propertyAddress,
                prsdUrl = prsdUrl,
            ),
        )
    }
}
