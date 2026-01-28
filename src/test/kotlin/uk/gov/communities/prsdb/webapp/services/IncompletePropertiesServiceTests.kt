package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperties
import uk.gov.communities.prsdb.webapp.database.entity.ReminderEmailSent
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.ReminderEmailSentRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.exceptions.TrackEmailSentException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData
import java.time.Instant
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class IncompletePropertiesServiceTests {
    @Mock
    private lateinit var mockLandlordIncompletePropertiesRepository: LandlordIncompletePropertiesRepository

    @Mock
    private lateinit var mockReminderEmailSentRepository: ReminderEmailSentRepository

    @Mock
    private lateinit var mockSavedJourneyStateRepository: SavedJourneyStateRepository

    @InjectMocks
    private lateinit var incompletePropertiesService: IncompletePropertiesService

    @Test
    fun `getIncompletePropertiesDueReminder retrieves all old properties if no reminders have been sent`() {
        // Arrange
        val landlord = MockLandlordData.createLandlord()
        val incompletePropertyCreatedDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now().minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS + 2L),
            )

        val landlordIncompleteProperties =
            listOf(
                LandlordIncompleteProperties(
                    landlord,
                    MockSavedJourneyStateData.createSavedJourneyState(createdDate = incompletePropertyCreatedDate),
                ),
                LandlordIncompleteProperties(
                    landlord,
                    MockSavedJourneyStateData.createSavedJourneyState(createdDate = incompletePropertyCreatedDate),
                ),
            )
        whenever(
            mockLandlordIncompletePropertiesRepository
                .findBySavedJourneyState_CreatedDateBefore(any()),
        ).thenReturn(landlordIncompleteProperties)

        // Act
        val result = incompletePropertiesService.getIncompletePropertiesDueReminder()

        // Assert
        assertEquals(landlordIncompleteProperties, result)

        val captor = argumentCaptor<java.time.Instant>()
        verify(mockLandlordIncompletePropertiesRepository).findBySavedJourneyState_CreatedDateBefore(captor.capture())
        val expectedInstant =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now().minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong()),
            )
        assertEquals(expectedInstant, captor.firstValue)
    }

    @Test
    fun `getIncompletePropertiesDueReminder excludes properties with reminders already sent`() {
        // Arrange
        val landlord = MockLandlordData.createLandlord()
        val incompletePropertyCreatedDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now().minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS + 2L),
            )

        val landlordIncompleteProperties =
            listOf(
                LandlordIncompleteProperties(
                    landlord,
                    MockSavedJourneyStateData.createSavedJourneyState(
                        createdDate = incompletePropertyCreatedDate,
                        reminderEmailSent = MockSavedJourneyStateData.createReminderEmailSent(),
                    ),
                ),
                LandlordIncompleteProperties(
                    landlord,
                    MockSavedJourneyStateData.createSavedJourneyState(createdDate = incompletePropertyCreatedDate),
                ),
            )
        whenever(
            mockLandlordIncompletePropertiesRepository
                .findBySavedJourneyState_CreatedDateBefore(any()),
        ).thenReturn(landlordIncompleteProperties)

        // Act
        val result = incompletePropertiesService.getIncompletePropertiesDueReminder()

        // Assert mapping
        assertEquals(listOf(landlordIncompleteProperties[1]), result)
    }

    @Test
    fun `recordReminderEmailSent saves ReminderEmailSent record with correct fields`() {
        // Arrange
        val incompletePropertySavedJourneyState = MockSavedJourneyStateData.createSavedJourneyState()

        // Act
        incompletePropertiesService.recordReminderEmailSent(incompletePropertySavedJourneyState)

        // Assert
        val reminderEmailSentCaptor = argumentCaptor<ReminderEmailSent>()
        verify(mockReminderEmailSentRepository).save(reminderEmailSentCaptor.capture())
        assertTrue(reminderEmailSentCaptor.firstValue.lastReminderEmailSentDate.isBefore(Instant.now().plusSeconds(1)))
        assertTrue(reminderEmailSentCaptor.firstValue.lastReminderEmailSentDate.isAfter(Instant.now().minusSeconds(600)))

        val savedJourneyStateCaptor = argumentCaptor<SavedJourneyState>()
        verify(mockSavedJourneyStateRepository).save(savedJourneyStateCaptor.capture())
        assertEquals(
            reminderEmailSentCaptor.firstValue,
            savedJourneyStateCaptor.firstValue.reminderEmailSent,
        )
    }

    @Test
    fun `recordReminderEmailSent throws TrackEmailSentException on failure`() {
        // Arrange
        val incompletePropertySavedJourneyState = MockSavedJourneyStateData.createSavedJourneyState()
        whenever(mockReminderEmailSentRepository.save(any()))
            .thenThrow(RuntimeException("Database error"))

        // Act & Assert
        assertThrows<TrackEmailSentException> { incompletePropertiesService.recordReminderEmailSent(incompletePropertySavedJourneyState) }
    }
}
