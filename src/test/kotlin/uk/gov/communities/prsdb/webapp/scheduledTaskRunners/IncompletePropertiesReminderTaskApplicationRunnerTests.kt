package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.application.IncompletePropertiesReminderTaskApplicationRunner
import uk.gov.communities.prsdb.webapp.application.IncompletePropertiesReminderTaskApplicationRunner.Companion.INCOMPLETE_PROPERTY_REMINDER_TASK_METHOD_NAME
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperties
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.IncompletePropertyReminderEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.IncompletePropertiesService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData
import java.io.ByteArrayOutputStream
import java.io.PrintStream
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
    private lateinit var incompletePropertiesService: IncompletePropertiesService

    @InjectMocks
    private lateinit var runner: IncompletePropertiesReminderTaskApplicationRunner

    @Test
    fun `incompletePropertiesReminderTaskLogic sends an email to the landlord for each incomplete property older than 21 days`() {
        // Arrange
        val mockPrsdUrl = "www.prsd-url.com/landlord/dashboard"
        val emailAddress1 = "user.one@example.com"
        val emailAddress2 = "user.two@example.com"
        val propertyAddress1 = "Address One"
        val propertyAddress2 = "Address Two"
        val daysToComplete = 7
        val createdDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now().minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong()),
            )

        val expectedEmail1 =
            IncompletePropertyReminderEmail(
                singleLineAddress = propertyAddress1,
                daysToComplete = daysToComplete,
                prsdUrl = mockPrsdUrl,
            )
        val expectedEmail2 =
            IncompletePropertyReminderEmail(
                singleLineAddress = propertyAddress2,
                daysToComplete = daysToComplete,
                prsdUrl = mockPrsdUrl,
            )

        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI(mockPrsdUrl))

        whenever(incompletePropertiesService.getOldIncompletePropertyRecordsWithNoReminderSent())
            .thenReturn(
                listOf(
                    LandlordIncompleteProperties(
                        landlord = MockLandlordData.createLandlord(email = emailAddress1),
                        savedJourneyState =
                            MockSavedJourneyStateData.createSavedJourneyState(
                                serializedState = MockSavedJourneyStateData.createSerialisedStateWithSingleLineAddress(propertyAddress1),
                                createdDate = createdDate,
                            ),
                    ),
                    LandlordIncompleteProperties(
                        landlord = MockLandlordData.createLandlord(email = emailAddress2),
                        savedJourneyState =
                            MockSavedJourneyStateData.createSavedJourneyState(
                                serializedState = MockSavedJourneyStateData.createSerialisedStateWithSingleLineAddress(propertyAddress2),
                                createdDate = createdDate,
                            ),
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
        verify(emailSender).sendEmail(emailAddress1, expectedEmail1)
        verify(emailSender).sendEmail(emailAddress2, expectedEmail2)
    }

    @Test
    fun `incompletePropertiesReminderTaskLogic still attempts other sends when one email fails, and completes task`() {
        // Arrange
        val mockPrsdUrl = "www.prsd-url.com/landlord/dashboard"
        val failingEmail = "fail@example.com"
        val succeedingEmail = "succeed@example.com"
        val addressFail = "Fail Address"
        val addressSucceed = "Succeed Address"
        val daysToComplete = 7
        val createdDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now().minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong()),
            )

        val expectedFailEmail =
            IncompletePropertyReminderEmail(
                singleLineAddress = addressFail,
                daysToComplete = daysToComplete,
                prsdUrl = mockPrsdUrl,
            )
        val expectedSucceedEmail =
            IncompletePropertyReminderEmail(
                singleLineAddress = addressSucceed,
                daysToComplete = daysToComplete,
                prsdUrl = mockPrsdUrl,
            )

        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI(mockPrsdUrl))
        whenever(incompletePropertiesService.getOldIncompletePropertyRecordsWithNoReminderSent())
            .thenReturn(
                listOf(
                    LandlordIncompleteProperties(
                        landlord = MockLandlordData.createLandlord(email = failingEmail),
                        savedJourneyState =
                            MockSavedJourneyStateData.createSavedJourneyState(
                                serializedState = MockSavedJourneyStateData.createSerialisedStateWithSingleLineAddress(addressFail),
                                createdDate = createdDate,
                            ),
                    ),
                    LandlordIncompleteProperties(
                        landlord = MockLandlordData.createLandlord(email = succeedingEmail),
                        savedJourneyState =
                            MockSavedJourneyStateData.createSavedJourneyState(
                                serializedState = MockSavedJourneyStateData.createSerialisedStateWithSingleLineAddress(addressSucceed),
                                createdDate = createdDate,
                            ),
                    ),
                ),
            )

        whenever(emailSender.sendEmail(failingEmail, expectedFailEmail))
            .doThrow(PersistentEmailSendException("Persistent failure"))

        val method =
            IncompletePropertiesReminderTaskApplicationRunner::class.java
                .getDeclaredMethod(INCOMPLETE_PROPERTY_REMINDER_TASK_METHOD_NAME)
        method.isAccessible = true

        // Act, capturing stdout
        // Assert does not throw and stdout contains expected messages
        val outContent = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outContent))
        try {
            assertDoesNotThrow {
                // Act
                method.invoke(runner)
            }

            val output = outContent.toString()
            assertTrue(output.contains("Email sent for incomplete property with savedJourneyStateId: "))
            assertTrue(output.contains("Task failed for incomplete property with savedJourneyStateId: "))
        } finally {
            System.setOut(originalOut)
        }

        verify(emailSender).sendEmail(failingEmail, expectedFailEmail)
        verify(emailSender).sendEmail(succeedingEmail, expectedSucceedEmail)
    }
}
