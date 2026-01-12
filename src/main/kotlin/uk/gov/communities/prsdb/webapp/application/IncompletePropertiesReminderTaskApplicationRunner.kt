package uk.gov.communities.prsdb.webapp.application

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.IncompletePropertyReminderEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import kotlin.system.exitProcess

@PrsdbScheduledTask("incomplete-property-reminder-scheduled-task")
class IncompletePropertiesReminderTaskApplicationRunner(
    private val context: ApplicationContext,
    private val emailSender: EmailNotificationService<IncompletePropertyReminderEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        println("Executing incomplete properties reminder scheduled task")

        // Separating into its own method to allow this to be tested without "exitProcess" being called
        incompletePropertiesReminderTaskLogic()

        val code =
            SpringApplication.exit(context, { 0 }).also {
                println("Scheduled task executed. Application will exit now.")
            }
        exitProcess(code)
    }

    private fun incompletePropertiesReminderTaskLogic() {
        // TODO - PRSD-1030 - retrieve incomplete properties older than 21 days
        val emailAddress = "jasmin.conterio@softwire.com"
        val propertyAddress = "HARDCODED ADDRESS"
        val daysToComplete = 7

        val prsdUrl = absoluteUrlProvider.buildLandlordDashboardUri().toString()

        emailSender.sendEmail(
            emailAddress,
            IncompletePropertyReminderEmail(
                singleLineAddress = propertyAddress,
                daysToComplete = daysToComplete,
                prsdUrl = prsdUrl,
            ),
        )
    }

    companion object {
        const val INCOMPLETE_PROPERTY_REMINDER_TASK_METHOD_NAME = "incompletePropertiesReminderTaskLogic"
    }
}
