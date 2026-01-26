package uk.gov.communities.prsdb.webapp.application

import kotlinx.datetime.daysUntil
import kotlinx.datetime.toKotlinLocalDate
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS
import uk.gov.communities.prsdb.webapp.helpers.CompleteByDateHelper
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.SavedJourneyStateHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.IncompletePropertyReminderEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.IncompletePropertiesService
import java.time.LocalDate
import kotlin.system.exitProcess

@PrsdbScheduledTask("incomplete-property-reminder-scheduled-task")
class IncompletePropertiesReminderTaskApplicationRunner(
    private val context: ApplicationContext,
    private val emailSender: EmailNotificationService<IncompletePropertyReminderEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val incompletePropertiesService: IncompletePropertiesService,
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
        val prsdUrl = absoluteUrlProvider.buildLandlordDashboardUri().toString()
        val cutoffDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now().minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong()),
            )

        val pagesOfProperties = incompletePropertiesService.getTotalPagesOfIncompletePropertiesOlderThanDate(cutoffDate)

        for (page in 0..<pagesOfProperties) {
            val incompleteProperties = incompletePropertiesService.getIncompletePropertiesDueReminderPage(cutoffDate, page)
            incompleteProperties.forEach { property ->
                try {
                    emailSender.sendEmail(
                        property.landlord.email,
                        IncompletePropertyReminderEmail(
                            singleLineAddress =
                                SavedJourneyStateHelper
                                    .getPropertyRegistrationSingleLineAddress(property.savedJourneyState.serializedState),
                            daysToComplete =
                                LocalDate.now().toKotlinLocalDate().daysUntil(
                                    CompleteByDateHelper.getIncompletePropertyCompleteByDateFromSavedJourneyState(
                                        property.savedJourneyState,
                                    ),
                                ),
                            prsdUrl = prsdUrl,
                        ),
                    )
                    println("Email sent for incomplete property with savedJourneyStateId: ${property.savedJourneyState.id}")
                    try {
                        incompletePropertiesService.recordReminderEmailSent(property.savedJourneyState)
                    } catch (ex: Exception) {
                        println(
                            "Failed to record reminder email sent for incomplete property with savedJourneyStateId: " +
                                property.savedJourneyState.id,
                        )
                        println("Exception message: ${ex.message}")
                        println("Stack trace: ${ex.stackTraceToString()}")
                    }
                } catch (ex: Exception) {
                    println("Task failed for incomplete property with savedJourneyStateId: ${property.savedJourneyState.id}")
                    println("Exception message: ${ex.message}")
                    println("Stack trace: ${ex.stackTraceToString()}")
                }
            }
        }
    }

    companion object {
        const val INCOMPLETE_PROPERTY_REMINDER_TASK_METHOD_NAME = "incompletePropertiesReminderTaskLogic"
    }
}
