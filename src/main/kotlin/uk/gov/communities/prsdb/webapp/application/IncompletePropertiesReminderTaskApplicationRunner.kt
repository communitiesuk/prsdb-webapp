package uk.gov.communities.prsdb.webapp.application

import kotlinx.datetime.daysUntil
import kotlinx.datetime.toKotlinLocalDate
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.IncompletePropertyReminderEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import java.time.LocalDate
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
        // TODO - PRSD-1030 - retrieve incomplete properties older than INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS days where no reminder email has yet been sent
        //
        //  Will need to check that createdDate is longer ago than INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS
        //  Will need to add something to the DB tracking if a reminder email has been sent for this incomplete property and only send if not yet sent
        //
        //   Include:
        //      landlord email address
        //      property address
        //      date property was created
        //  TEMP hardcoded values below
        val emailAddress = "jasmin.conterio@softwire.com"
        val propertyAddress = "HARDCODED ADDRESS"
        val completeByDate = LocalDate.now().plusDays(7L).toKotlinLocalDate()

        val prsdUrl = absoluteUrlProvider.buildLandlordDashboardUri().toString()

        // TODO - PRSD-1030. For each incomplete property retrieved from the DB, send email as below
        emailSender.sendEmail(
            emailAddress,
            IncompletePropertyReminderEmail(
                singleLineAddress = propertyAddress,
                daysToComplete = LocalDate.now().toKotlinLocalDate().daysUntil(completeByDate),
                prsdUrl = prsdUrl,
            ),
        )
    }

    companion object {
        const val INCOMPLETE_PROPERTY_REMINDER_TASK_METHOD_NAME = "incompletePropertiesReminderTaskLogic"
    }
}
