package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

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
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.IncompletePropertyReminderEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.NotifyEmailNotificationService
import java.net.URI

@ExtendWith(MockitoExtension::class)
class IncompletePropertiesReminderTaskApplicationRunnerTests {
    @Mock
    private lateinit var context: ApplicationContext

    @Mock
    lateinit var emailSender: NotifyEmailNotificationService<IncompletePropertyReminderEmail>

    @Mock
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @InjectMocks
    private lateinit var runner: IncompletePropertiesReminderTaskApplicationRunner

    @Test
    fun `incompletePropertiesReminderTaskLogic sends email to landlord`() {
        // Arrange
        val mockPrsdUrl = "www.prsd-url.com/landlord/dashboard"
        val expectedEmailModel =
            IncompletePropertyReminderEmail(
                singleLineAddress = "HARDCODED ADDRESS",
                prsdUrl = mockPrsdUrl,
            )

        whenever(absoluteUrlProvider.buildLandlordDashboardUri())
            .thenReturn(URI(mockPrsdUrl))

        val method =
            IncompletePropertiesReminderTaskApplicationRunner::class.java
                .getDeclaredMethod(INCOMPLETE_PROPERTY_REMINDER_TASK_METHOD_NAME)
        method.isAccessible = true

        // Act
        method.invoke(runner)

        // Assert
        verify(emailSender).sendEmail(
            "jasmin.conterio@softwire.com",
            expectedEmailModel,
        )
    }
}
