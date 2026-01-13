package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import kotlinx.datetime.toKotlinLocalDate
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.application.IncompletePropertiesReminderTaskApplicationRunner
import uk.gov.communities.prsdb.webapp.application.IncompletePropertiesReminderTaskApplicationRunner.Companion.INCOMPLETE_PROPERTY_REMINDER_TASK_METHOD_NAME
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompletePropertiesForReminderDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.IncompletePropertyReminderEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LandlordIncompletePropertiesService
import java.net.URI
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class IncompletePropertiesReminderTaskApplicationRunnerTests {
    @Mock
    private lateinit var context: ApplicationContext

    @Mock
    private lateinit var emailSender: EmailNotificationService<IncompletePropertyReminderEmail>

    @Mock
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var landlordIncompletePropertiesService: LandlordIncompletePropertiesService

    @InjectMocks
    private lateinit var runner: IncompletePropertiesReminderTaskApplicationRunner

    @Test
    fun `incompletePropertiesReminderTaskLogic sends email to landlord`() {
        // Arrange
        val mockPrsdUrl = "www.prsd-url.com/landlord/dashboard"
        val emailAddress = "user.name@example.com"
        val propertyAddress = "Single Line Address"
        val daysToComplete = 7
        val completeByDate = LocalDate.now().plusDays(daysToComplete.toLong()).toKotlinLocalDate()
        val expectedEmailModel =
            IncompletePropertyReminderEmail(
                singleLineAddress = propertyAddress,
                daysToComplete = daysToComplete,
                prsdUrl = mockPrsdUrl,
            )

        whenever(absoluteUrlProvider.buildLandlordDashboardUri())
            .thenReturn(URI(mockPrsdUrl))
        whenever(
            landlordIncompletePropertiesService
                .getIncompletePropertiesOlderThanDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS),
        ).thenReturn(
            listOf(
                IncompletePropertiesForReminderDataModel(
                    landlordEmail = emailAddress,
                    propertySingleLineAddress = propertyAddress,
                    completeByDate = completeByDate,
                ),
            ),
        )

        val method =
            IncompletePropertiesReminderTaskApplicationRunner::class.java
                .getDeclaredMethod(INCOMPLETE_PROPERTY_REMINDER_TASK_METHOD_NAME)
        method.isAccessible = true

        // Act
        method.invoke(runner)

        // Assert
        verify(emailSender).sendEmail(
            emailAddress,
            expectedEmailModel,
        )
    }
}
