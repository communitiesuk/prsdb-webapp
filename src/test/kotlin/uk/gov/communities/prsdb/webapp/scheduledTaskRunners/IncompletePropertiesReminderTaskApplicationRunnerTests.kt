package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationContext
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.application.IncompletePropertiesReminderTaskApplicationRunner
import uk.gov.communities.prsdb.webapp.application.IncompletePropertiesReminderTaskApplicationRunner.Companion.INCOMPLETE_PROPERTY_REMINDER_TASK_METHOD_NAME
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.IncompletePropertyReminderEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.NotifyEmailNotificationService
import java.net.URI

class IncompletePropertiesReminderTaskApplicationRunnerTests {
    @MockitoBean
    private lateinit var context: ApplicationContext

    @MockitoBean
    lateinit var emailSender: NotifyEmailNotificationService<IncompletePropertyReminderEmail>

    @MockitoBean
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @BeforeEach
    fun setUp() {
        context = mock()
        emailSender = mock()
        absoluteUrlProvider = mock()
    }

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

        val runner = IncompletePropertiesReminderTaskApplicationRunner(context, emailSender, absoluteUrlProvider)
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
