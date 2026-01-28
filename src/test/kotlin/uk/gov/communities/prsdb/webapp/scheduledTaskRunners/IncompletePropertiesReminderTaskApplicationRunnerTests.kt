package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.application.IncompletePropertiesReminderTaskApplicationRunner
import uk.gov.communities.prsdb.webapp.application.IncompletePropertiesReminderTaskApplicationRunner.Companion.INCOMPLETE_PROPERTY_REMINDER_TASK_METHOD_NAME
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperties
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.exceptions.TrackEmailSentException
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

    private val mockPrsdUrl = "www.prsd-url.com/landlord/dashboard"

    private val incompletePropertyReminderTaskMethod =
        IncompletePropertiesReminderTaskApplicationRunner::class.java
            .getDeclaredMethod(INCOMPLETE_PROPERTY_REMINDER_TASK_METHOD_NAME)
    private val emailAddress1 = "user.one@example.com"
    private val emailAddress2 = "user.two@example.com"
    private lateinit var reminderEmail1: IncompletePropertyReminderEmail
    private lateinit var reminderEmail2: IncompletePropertyReminderEmail
    private lateinit var savedJourneyState1: SavedJourneyState
    private lateinit var savedJourneyState2: SavedJourneyState

    @BeforeEach
    fun setUp() {
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI(mockPrsdUrl))

        incompletePropertyReminderTaskMethod.isAccessible = true
    }

    @Test
    fun `incompletePropertiesReminderTaskLogic sends an email to the landlord for each incomplete property older than 21 days`() {
        // Arrange
        setupTwoEmailsToSend()

        // Act
        incompletePropertyReminderTaskMethod.invoke(runner)

        // Assert
        verify(emailSender).sendEmail(emailAddress1, reminderEmail1)
        verify(emailSender).sendEmail(emailAddress2, reminderEmail2)
    }

    @Test
    fun `incompletePropertiesReminderTaskLogic still attempts other sends when one email fails, and completes task`() {
        // Arrange
        setupTwoEmailsToSend()
        val expectedFailEmail = reminderEmail1
        val expectedSucceedEmail = reminderEmail2

        whenever(emailSender.sendEmail(emailAddress1, expectedFailEmail))
            .doThrow(PersistentEmailSendException("Persistent email failure"))

        // Act, capturing stdout
        // Assert does not throw and stdout contains expected messages
        val outContent = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outContent))
        try {
            assertDoesNotThrow {
                // Act
                incompletePropertyReminderTaskMethod.invoke(runner)
            }

            val output = outContent.toString()
            assertTrue(output.contains("Email sent for incomplete property with savedJourneyStateId: 2"))
            assertTrue(output.contains("Failed to send reminder email for incomplete property with savedJourneyStateId: 1"))
        } finally {
            System.setOut(originalOut)
        }

        verify(emailSender).sendEmail(emailAddress1, expectedFailEmail)
        verify(emailSender).sendEmail(emailAddress2, expectedSucceedEmail)
    }

    @Test
    fun `incompletePropertiesReminderTaskLogic records reminder email sent when email is sent`() {
        // Arrange
        setupTwoEmailsToSend()

        // Act
        incompletePropertyReminderTaskMethod.invoke(runner)

        // Assert
        verify(incompletePropertiesService).recordReminderEmailSent(savedJourneyState1)
        verify(incompletePropertiesService).recordReminderEmailSent(savedJourneyState2)
    }

    @Test
    fun `incompletePropertiesReminderTaskLogic prints error then continues to next send if recording email sent fails`() {
        // Arrange
        setupTwoEmailsToSend()

        whenever(incompletePropertiesService.recordReminderEmailSent(savedJourneyState1))
            .doThrow(TrackEmailSentException("Database error"))

        // Act, capturing stdout
        // Assert does not throw and stdout contains expected messages
        val outContent = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outContent))
        try {
            assertDoesNotThrow {
                // Act
                incompletePropertyReminderTaskMethod.invoke(runner)
            }

            val output = outContent.toString()
            assertTrue(output.contains("Email sent for incomplete property with savedJourneyStateId: 1"))
            assertTrue(output.contains("Email sent for incomplete property with savedJourneyStateId: 2"))
            assertTrue(output.contains("Failed to record reminder email sent for incomplete property with savedJourneyStateId: 1"))
        } finally {
            System.setOut(originalOut)
        }

        verify(emailSender).sendEmail(emailAddress1, reminderEmail1)
        verify(emailSender).sendEmail(emailAddress2, reminderEmail2)
        verify(incompletePropertiesService).recordReminderEmailSent(savedJourneyState1)
        verify(incompletePropertiesService).recordReminderEmailSent(savedJourneyState2)
    }

    @Test
    fun `incompletePropertiesReminderTaskLogic does not try to record the email sent if email sending fails`() {
        // Arrange
        setupTwoEmailsToSend()

        whenever(emailSender.sendEmail(emailAddress1, reminderEmail1))
            .doThrow(PersistentEmailSendException("Persistent email failure"))

        // Act
        incompletePropertyReminderTaskMethod.invoke(runner)

        // Assert
        verify(emailSender).sendEmail(emailAddress1, reminderEmail1)
        verify(emailSender).sendEmail(emailAddress2, reminderEmail2)
        verify(incompletePropertiesService, never()).recordReminderEmailSent(savedJourneyState1)
        verify(incompletePropertiesService).recordReminderEmailSent(savedJourneyState2)
    }

    private fun setupTwoEmailsToSend() {
        val propertyAddress1 = "Address One"
        val propertyAddress2 = "Address Two"
        val daysToComplete = 7
        val createdDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now().minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong()),
            )

        reminderEmail1 =
            IncompletePropertyReminderEmail(
                singleLineAddress = propertyAddress1,
                daysToComplete = daysToComplete,
                prsdUrl = mockPrsdUrl,
            )

        reminderEmail2 =
            IncompletePropertyReminderEmail(
                singleLineAddress = propertyAddress2,
                daysToComplete = daysToComplete,
                prsdUrl = mockPrsdUrl,
            )

        savedJourneyState1 =
            MockSavedJourneyStateData.createSavedJourneyState(
                serializedState = MockSavedJourneyStateData.createSerialisedStateWithSingleLineAddress(propertyAddress1),
                createdDate = createdDate,
                entityId = 1L,
            )

        savedJourneyState2 =
            MockSavedJourneyStateData.createSavedJourneyState(
                serializedState = MockSavedJourneyStateData.createSerialisedStateWithSingleLineAddress(propertyAddress2),
                createdDate = createdDate,
                entityId = 2L,
            )

        whenever(incompletePropertiesService.getIncompletePropertiesDueReminder())
            .thenReturn(
                listOf(
                    LandlordIncompleteProperties(
                        landlord = MockLandlordData.createLandlord(email = emailAddress1),
                        savedJourneyState = savedJourneyState1,
                    ),
                    LandlordIncompleteProperties(
                        landlord = MockLandlordData.createLandlord(email = emailAddress2),
                        savedJourneyState = savedJourneyState2,
                    ),
                ),
            )
    }
}
