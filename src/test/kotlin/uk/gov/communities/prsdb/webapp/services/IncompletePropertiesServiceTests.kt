package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageRequest
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.MAX_INCOMPLETE_PROPERTIES_FROM_DATABASE
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperties
import uk.gov.communities.prsdb.webapp.database.entity.ReminderEmailSent
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.ReminderEmailSentRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
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
    private lateinit var mockSavedJourneyStateRepository: SavedJourneyStateRepository

    @Mock
    private lateinit var mockReminderEmailSentRepository: ReminderEmailSentRepository

    @Mock
    private lateinit var mockSavedJourneyStateRepository: SavedJourneyStateRepository

    @InjectMocks
    private lateinit var incompletePropertiesService: IncompletePropertiesService

    @Nested
    inner class GetIncompletePropertiesDueReminderPageTests {
        private val landlord = MockLandlordData.createLandlord()
        private val incompletePropertyCreatedDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now().minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS + 2L),
            )
        private val reminderCutoffDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now().minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong()),
            )
        private val pageRequest = PageRequest.of(0, MAX_INCOMPLETE_PROPERTIES_FROM_DATABASE)

        @Test
        fun `getIncompletePropertiesDueReminderPage retrieves a page of old properties if no reminders have been sent`() {
            // Arrange
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
                    .findBySavedJourneyState_CreatedDateBefore(reminderCutoffDate, pageRequest),
            ).thenReturn(landlordIncompleteProperties)

            // Act
            val result = incompletePropertiesService.getIncompletePropertiesDueReminderPage(reminderCutoffDate, 0)

            // Assert
            assertEquals(landlordIncompleteProperties, result)

            val captor = argumentCaptor<java.time.Instant>()
            verify(mockLandlordIncompletePropertiesRepository).findBySavedJourneyState_CreatedDateBefore(captor.capture(), eq(pageRequest))
            val expectedInstant =
                DateTimeHelper.getJavaInstantFromLocalDate(
                    LocalDate.now().minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong()),
                )
            assertEquals(expectedInstant, captor.firstValue)
        }

        @Test
        fun `getIncompletePropertiesDueReminderPage excludes properties with reminders already sent`() {
            // Arrange
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
                    .findBySavedJourneyState_CreatedDateBefore(reminderCutoffDate, pageRequest),
            ).thenReturn(landlordIncompleteProperties)

            // Act
            val result = incompletePropertiesService.getIncompletePropertiesDueReminderPage(reminderCutoffDate, 0)

            // Assert mapping
            assertEquals(listOf(landlordIncompleteProperties[1]), result)
        }
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

    @Nested
    inner class DeleteIncompletePropertiesOlderThan28Days {
        private val landlord = MockLandlordData.createLandlord()

        private val incompletePropertyCreatedDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now().minusDays(30),
            )
        private val savedJourneyState1 =
            MockSavedJourneyStateData.createSavedJourneyState(
                journeyId = "journey-1",
                createdDate = incompletePropertyCreatedDate,
            )
        private val savedJourneyState2 =
            MockSavedJourneyStateData.createSavedJourneyState(
                journeyId = "journey-2",
                createdDate = incompletePropertyCreatedDate,
            )

        private val cutoffDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now().minusDays(28),
            )

        @Test
        fun `deleteIncompletePropertiesOlderThan28Days deletes records and returns count`() {
            // Arrange
            val pageRequest = PageRequest.of(0, MAX_INCOMPLETE_PROPERTIES_FROM_DATABASE)
            whenever(mockLandlordIncompletePropertiesRepository.countBySavedJourneyState_CreatedDateBefore(cutoffDate))
                .thenReturn(2L)

            val incompletePropertyEntities =
                listOf(
                    LandlordIncompleteProperties(landlord, savedJourneyState1),
                    LandlordIncompleteProperties(landlord, savedJourneyState2),
                )
            whenever(
                mockLandlordIncompletePropertiesRepository
                    .findBySavedJourneyState_CreatedDateBefore(cutoffDate, pageRequest),
            ).thenReturn(incompletePropertyEntities)

            // Act
            val deletedCount = incompletePropertiesService.deleteIncompletePropertiesOlderThan28Days()

            // Assert deleteAll called with correct SavedJourneyStates
            val captor = argumentCaptor<List<SavedJourneyState>>()
            verify(mockLandlordIncompletePropertiesRepository).findBySavedJourneyState_CreatedDateBefore(cutoffDate, pageRequest)
            verify(mockSavedJourneyStateRepository).deleteAll(captor.capture())
            val deletedSavedJourneyStates = captor.firstValue
            assertEquals(2, deletedSavedJourneyStates.size)
            assertEquals(2, deletedCount)
            assert(deletedSavedJourneyStates.any { it.journeyId == "journey-1" })
            assert(deletedSavedJourneyStates.any { it.journeyId == "journey-2" })
        }

        @Test
        fun `deleteIncompletePropertiesOlderThan28Days processes multiple pages of database records`() {
            // Arrange
            val pageRequest1 = PageRequest.of(0, MAX_INCOMPLETE_PROPERTIES_FROM_DATABASE)
            val pageRequest2 = PageRequest.of(1, MAX_INCOMPLETE_PROPERTIES_FROM_DATABASE)
            whenever(mockLandlordIncompletePropertiesRepository.countBySavedJourneyState_CreatedDateBefore(cutoffDate))
                .thenReturn((MAX_INCOMPLETE_PROPERTIES_FROM_DATABASE + 1).toLong())

            val incompletePropertyEntitiesPage1 = listOf(LandlordIncompleteProperties(landlord, savedJourneyState1))
            val incompletePropertyEntitiesPage2 = listOf(LandlordIncompleteProperties(landlord, savedJourneyState2))
            whenever(
                mockLandlordIncompletePropertiesRepository
                    .findBySavedJourneyState_CreatedDateBefore(cutoffDate, pageRequest1),
            ).thenReturn(incompletePropertyEntitiesPage1)
            whenever(
                mockLandlordIncompletePropertiesRepository
                    .findBySavedJourneyState_CreatedDateBefore(cutoffDate, pageRequest2),
            ).thenReturn(incompletePropertyEntitiesPage2)

            // Act
            val deletedCount = incompletePropertiesService.deleteIncompletePropertiesOlderThan28Days()

            // Assert
            assertEquals(2, deletedCount)
            verify(mockSavedJourneyStateRepository, times(2)).deleteAll(any())
        }
    }

    @Test
    fun `getTotalPagesOfIncompletePropertiesOlderThanDate calculates total pages correctly`() {
        // Arrange
        val cutoffDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now().minusDays(28),
            )

        whenever(mockLandlordIncompletePropertiesRepository.countBySavedJourneyState_CreatedDateBefore(cutoffDate))
            .thenReturn(MAX_INCOMPLETE_PROPERTIES_FROM_DATABASE + 1L)

        // Act
        val totalPages = incompletePropertiesService.getTotalPagesOfIncompletePropertiesOlderThanDate(cutoffDate)

        // Assert
        assertEquals(2, totalPages)
    }
}
