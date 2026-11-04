package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.application.IncompletePropertiesReminderTaskLogic
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperty
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.exceptions.TrackEmailSentException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.IncompletePropertyReminderEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.IncompletePropertiesService
import uk.gov.communities.prsdb.webapp.services.LandlordUserEmailService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
class IncompletePropertiesReminderTaskApplicationRunnerTests {
    @Mock
    private lateinit var emailSender: EmailNotificationService<IncompletePropertyReminderEmail>

    @Mock
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var incompletePropertiesService: IncompletePropertiesService

    @Mock
    private lateinit var landlordUserEmailService: LandlordUserEmailService

    private lateinit var taskLogic: IncompletePropertiesReminderTaskLogic

    private val mockPrsdUrl = "www.prsd-url.com/landlord/dashboard"

    private val emailAddress1 = "user.one@example.com"
    private val emailAddress2 = "user.two@example.com"
    private lateinit var reminderEmail1: IncompletePropertyReminderEmail
    private lateinit var reminderEmail2: IncompletePropertyReminderEmail
    private lateinit var savedJourneyState1: SavedJourneyState
    private lateinit var savedJourneyState2: SavedJourneyState

    @BeforeEach
    fun setUp() {
        taskLogic = createTaskLogic()
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI(mockPrsdUrl))
    }

    private fun createTaskLogic(dateTimeHelper: DateTimeHelper = DateTimeHelper()) =
        IncompletePropertiesReminderTaskLogic(
            emailSender,
            absoluteUrlProvider,
            incompletePropertiesService,
            landlordUserEmailService,
            dateTimeHelper,
        )

    @Test
    fun `sendIncompletePropertyReminders sends an email to the landlord for each incomplete property older than 21 days`() {
        // Arrange
        setupTwoEmailsToSend()
        setupTwoEntriesOnOneDatabasePage()

        // Act
        taskLogic.sendIncompletePropertyReminders()

        // Assert
        verify(emailSender).sendEmail(emailAddress1, reminderEmail1)
        verify(emailSender).sendEmail(emailAddress2, reminderEmail2)
    }

    @Test
    fun `sendIncompletePropertyReminders still attempts other sends when one email fails, and completes task`() {
        // Arrange
        setupTwoEmailsToSend()
        setupTwoEntriesOnOneDatabasePage()
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
                taskLogic.sendIncompletePropertyReminders()
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
    fun `sendIncompletePropertyReminders records reminder email sent when email is sent`() {
        // Arrange
        setupTwoEmailsToSend()
        setupTwoEntriesOnOneDatabasePage()

        // Act
        taskLogic.sendIncompletePropertyReminders()

        // Assert
        verify(incompletePropertiesService).recordReminderEmailSent(savedJourneyState1)
        verify(incompletePropertiesService).recordReminderEmailSent(savedJourneyState2)
    }

    @Test
    fun `sendIncompletePropertyReminders prints error then continues to next send if recording email sent fails`() {
        // Arrange
        setupTwoEmailsToSend()
        setupTwoEntriesOnOneDatabasePage()

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
                taskLogic.sendIncompletePropertyReminders()
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
    fun `sendIncompletePropertyReminders does not try to record the email sent if email sending fails`() {
        // Arrange
        setupTwoEmailsToSend()
        setupTwoEntriesOnOneDatabasePage()

        whenever(emailSender.sendEmail(emailAddress1, reminderEmail1))
            .doThrow(PersistentEmailSendException("Persistent email failure"))

        // Act
        taskLogic.sendIncompletePropertyReminders()

        // Assert
        verify(emailSender).sendEmail(emailAddress1, reminderEmail1)
        verify(emailSender).sendEmail(emailAddress2, reminderEmail2)
        verify(incompletePropertiesService, never()).recordReminderEmailSent(savedJourneyState1)
        verify(incompletePropertiesService).recordReminderEmailSent(savedJourneyState2)
    }

    @Test
    fun `sendIncompletePropertyReminders processes multiple database pages of incomplete properties`() {
        // Arrange
        setupTwoEmailsToSend()
        setupTwoEntriesOnTwoDatabasePages()

        // Act
        taskLogic.sendIncompletePropertyReminders()

        // Assert
        verify(incompletePropertiesService, times(2)).getIncompletePropertiesDueReminderPage(any(), any())
        verify(emailSender).sendEmail(emailAddress1, reminderEmail1)
        verify(emailSender).sendEmail(emailAddress2, reminderEmail2)
        verify(incompletePropertiesService).recordReminderEmailSent(savedJourneyState1)
        verify(incompletePropertiesService).recordReminderEmailSent(savedJourneyState2)
    }

    @Test
    fun `sendIncompletePropertyReminders looks up emails once per page rather than once per property`() {
        // Arrange
        setupTwoEmailsToSend()
        setupTwoEntriesOnOneDatabasePage()

        // Act
        taskLogic.sendIncompletePropertyReminders()

        // Assert
        verify(landlordUserEmailService, times(1)).getEmailsByBaseUserId(any())
    }

    @Test
    fun `sendIncompletePropertyReminders prints an error then continues if no email is found for the user`() {
        // Arrange
        setupTwoEmailsToSend()
        val reminderCutoffDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now(DateTimeHelper.UK_ZONE).minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong()),
            )
        val userWithoutEmail = MockLandlordData.createPrsdbUser("user-without-email")
        val user2 = MockLandlordData.createPrsdbUser("user-2")

        whenever(incompletePropertiesService.getNumberOfPagesOfIncompletePropertiesOlderThanDate(reminderCutoffDate)).thenReturn(1)
        whenever(incompletePropertiesService.getIncompletePropertiesDueReminderPage(reminderCutoffDate))
            .thenReturn(
                listOf(
                    LandlordIncompleteProperty(
                        user = userWithoutEmail,
                        savedJourneyState = savedJourneyState1,
                    ),
                    LandlordIncompleteProperty(
                        user = user2,
                        savedJourneyState = savedJourneyState2,
                    ),
                ),
            )
        whenever(landlordUserEmailService.getEmailsByBaseUserId(listOf(userWithoutEmail.id, user2.id)))
            .thenReturn(mapOf(user2.id to emailAddress2))

        // Act, capturing stdout
        // Assert does not throw and stdout contains expected messages
        val outContent = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outContent))
        try {
            assertDoesNotThrow {
                // Act
                taskLogic.sendIncompletePropertyReminders()
            }

            val output = outContent.toString()
            assertTrue(
                output.contains("No email address found for the user who started incomplete property with savedJourneyStateId: 1"),
            )
        } finally {
            System.setOut(originalOut)
        }

        verify(emailSender, never()).sendEmail(eq(emailAddress1), any())
        verify(emailSender).sendEmail(emailAddress2, reminderEmail2)
        verify(incompletePropertiesService, never()).recordReminderEmailSent(savedJourneyState1)
        verify(incompletePropertiesService).recordReminderEmailSent(savedJourneyState2)
    }

    @Test
    fun `sendIncompletePropertyReminders returns zero failures when all emails are sent`() {
        // Arrange
        setupTwoEmailsToSend()
        setupTwoEntriesOnOneDatabasePage()

        // Act
        val failureCount = taskLogic.sendIncompletePropertyReminders()

        // Assert
        assertEquals(0, failureCount)
    }

    @Test
    fun `sendIncompletePropertyReminders returns a non-zero failure count when an email send fails`() {
        // Arrange
        setupTwoEmailsToSend()
        setupTwoEntriesOnOneDatabasePage()

        whenever(emailSender.sendEmail(emailAddress1, reminderEmail1))
            .doThrow(PersistentEmailSendException("Persistent email failure"))

        // Act
        val failureCount = taskLogic.sendIncompletePropertyReminders()

        // Assert
        assertEquals(1, failureCount)
    }

    @Test
    fun `sendIncompletePropertyReminders returns a non-zero failure count when recording email sent fails`() {
        // Arrange
        setupTwoEmailsToSend()
        setupTwoEntriesOnOneDatabasePage()

        whenever(incompletePropertiesService.recordReminderEmailSent(savedJourneyState1))
            .doThrow(TrackEmailSentException("Database error"))

        // Act
        val failureCount = taskLogic.sendIncompletePropertyReminders()

        // Assert
        assertEquals(1, failureCount)
    }

    @Test
    fun `sendIncompletePropertyReminders returns a non-zero failure count when no email is found for the user`() {
        // Arrange
        setupTwoEmailsToSend()
        val reminderCutoffDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now(DateTimeHelper.UK_ZONE).minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong()),
            )
        val userWithoutEmail = MockLandlordData.createPrsdbUser("user-without-email")
        val user2 = MockLandlordData.createPrsdbUser("user-2")

        whenever(incompletePropertiesService.getNumberOfPagesOfIncompletePropertiesOlderThanDate(reminderCutoffDate)).thenReturn(1)
        whenever(incompletePropertiesService.getIncompletePropertiesDueReminderPage(reminderCutoffDate))
            .thenReturn(
                listOf(
                    LandlordIncompleteProperty(
                        user = userWithoutEmail,
                        savedJourneyState = savedJourneyState1,
                    ),
                    LandlordIncompleteProperty(
                        user = user2,
                        savedJourneyState = savedJourneyState2,
                    ),
                ),
            )
        whenever(landlordUserEmailService.getEmailsByBaseUserId(listOf(userWithoutEmail.id, user2.id)))
            .thenReturn(mapOf(user2.id to emailAddress2))

        // Act
        val failureCount = taskLogic.sendIncompletePropertyReminders()

        // Assert
        assertEquals(1, failureCount)
    }

    @Test
    fun `sendIncompletePropertyReminders uses the UK date when the UK has already reached the next day`() {
        // Arrange
        // 23:30 UTC on 15 June 2026 is 00:30 BST on 16 June 2026 in the UK
        val taskLogicJustAfterUkMidnight =
            createTaskLogic(
                DateTimeHelper(Clock.fixed(Instant.parse("2026-06-15T23:30:00Z"), ZoneOffset.UTC)),
            )
        val expectedCutoffDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.of(2026, 6, 16).minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong()),
            )
        val propertyAddress = "Address One"
        // Started on 26 May 2026 (UK), so it must be completed by 23 June 2026: 7 days after 16 June
        val savedJourneyState =
            MockSavedJourneyStateData.createSavedJourneyState(
                serializedState = MockSavedJourneyStateData.createSerialisedStateWithSingleLineAddress(propertyAddress),
                createdDate = Instant.parse("2026-05-26T10:00:00Z"),
                entityId = 1L,
            )
        val user = MockLandlordData.createPrsdbUser("user-1")

        whenever(incompletePropertiesService.getNumberOfPagesOfIncompletePropertiesOlderThanDate(expectedCutoffDate)).thenReturn(1)
        whenever(incompletePropertiesService.getIncompletePropertiesDueReminderPage(expectedCutoffDate))
            .thenReturn(listOf(LandlordIncompleteProperty(user = user, savedJourneyState = savedJourneyState)))
        whenever(landlordUserEmailService.getEmailsByBaseUserId(listOf(user.id)))
            .thenReturn(mapOf(user.id to emailAddress1))

        // Act
        taskLogicJustAfterUkMidnight.sendIncompletePropertyReminders()

        // Assert
        verify(emailSender).sendEmail(
            emailAddress1,
            IncompletePropertyReminderEmail(
                singleLineAddress = propertyAddress,
                daysToComplete = 7,
                prsdUrl = mockPrsdUrl,
            ),
        )
    }

    private fun setupTwoEmailsToSend() {
        val propertyAddress1 = "Address One"
        val propertyAddress2 = "Address Two"
        val daysToComplete = 7
        val createdDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now(DateTimeHelper.UK_ZONE).minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong()),
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
    }

    private fun setupTwoEntriesOnOneDatabasePage() {
        val reminderCutoffDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now(DateTimeHelper.UK_ZONE).minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong()),
            )

        whenever(incompletePropertiesService.getNumberOfPagesOfIncompletePropertiesOlderThanDate(reminderCutoffDate)).thenReturn(
            1,
        )

        val user1 = MockLandlordData.createPrsdbUser("user-1")
        val user2 = MockLandlordData.createPrsdbUser("user-2")

        whenever(landlordUserEmailService.getEmailsByBaseUserId(listOf(user1.id, user2.id)))
            .thenReturn(mapOf(user1.id to emailAddress1, user2.id to emailAddress2))

        whenever(incompletePropertiesService.getIncompletePropertiesDueReminderPage(reminderCutoffDate))
            .thenReturn(
                listOf(
                    LandlordIncompleteProperty(
                        user = user1,
                        savedJourneyState = savedJourneyState1,
                    ),
                    LandlordIncompleteProperty(
                        user = user2,
                        savedJourneyState = savedJourneyState2,
                    ),
                ),
            )
    }

    private fun setupTwoEntriesOnTwoDatabasePages() {
        val reminderCutoffDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now(DateTimeHelper.UK_ZONE).minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong()),
            )

        whenever(incompletePropertiesService.getNumberOfPagesOfIncompletePropertiesOlderThanDate(reminderCutoffDate)).thenReturn(
            2,
        )

        val user1 = MockLandlordData.createPrsdbUser("user-1")
        val user2 = MockLandlordData.createPrsdbUser("user-2")

        whenever(landlordUserEmailService.getEmailsByBaseUserId(listOf(user1.id)))
            .thenReturn(mapOf(user1.id to emailAddress1))
        whenever(landlordUserEmailService.getEmailsByBaseUserId(listOf(user2.id)))
            .thenReturn(mapOf(user2.id to emailAddress2))

        whenever(incompletePropertiesService.getIncompletePropertiesDueReminderPage(reminderCutoffDate, 0))
            .thenReturn(
                listOf(
                    LandlordIncompleteProperty(
                        user = user1,
                        savedJourneyState = savedJourneyState1,
                    ),
                ),
            )

        whenever(incompletePropertiesService.getIncompletePropertiesDueReminderPage(reminderCutoffDate, 1))
            .thenReturn(
                listOf(
                    LandlordIncompleteProperty(
                        user = user2,
                        savedJourneyState = savedJourneyState2,
                    ),
                ),
            )
    }
}
