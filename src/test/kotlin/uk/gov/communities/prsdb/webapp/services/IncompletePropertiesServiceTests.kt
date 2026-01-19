package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.helpers.CompleteByDateHelper
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompletePropertyForReminderDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData.Companion.createLandlordIncompleteProperties
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class IncompletePropertiesServiceTests {
    @Mock
    private lateinit var mockLandlordIncompletePropertiesRepository: LandlordIncompletePropertiesRepository

    @InjectMocks
    private lateinit var incompletePropertiesService: IncompletePropertiesService

    @Test
    fun `getIncompletePropertyReminders retrieves incomplete properties and maps fields`() {
        // Arrange
        val landlord =
            MockLandlordData.createLandlord(
                email = "user.name@example.com",
            )
        val incompletePropertyCreatedDate =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now().minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS + 2L),
            )
        val savedJourneyState =
            MockSavedJourneyStateData.createSavedJourneyState(
                journeyId = "journey-123",
                serializedState = MockSavedJourneyStateData.createSerialisedStateWithSingleLineAddress("1 Test Street"),
                createdDate = incompletePropertyCreatedDate,
            )

        val incompletePropertyEntities = createLandlordIncompleteProperties(landlord, savedJourneyState)
        whenever(
            mockLandlordIncompletePropertiesRepository
                .findBySavedJourneyState_CreatedDateBefore(any()),
        ).thenReturn(listOf(incompletePropertyEntities))

        val expectedCompleteByDate = CompleteByDateHelper.getIncompletePropertyCompleteByDateFromCreatedDate(incompletePropertyCreatedDate)

        val expectedIncompletePropertyForReminderDataModel =
            IncompletePropertyForReminderDataModel(
                landlordEmail = "user.name@example.com",
                propertySingleLineAddress = "1 Test Street",
                completeByDate = expectedCompleteByDate,
                savedJourneyStateId = "journey-123",
            )

        // Act
        val result = incompletePropertiesService.getIncompletePropertyReminders()

        // Assert mapping
        assertEquals(listOf(expectedIncompletePropertyForReminderDataModel), result)

        // Assert repository called with expected Instant cutoff
        val captor = argumentCaptor<java.time.Instant>()
        verify(mockLandlordIncompletePropertiesRepository).findBySavedJourneyState_CreatedDateBefore(captor.capture())
        val expectedInstant =
            DateTimeHelper.getJavaInstantFromLocalDate(
                LocalDate.now().minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong()),
            )
        assertEquals(expectedInstant, captor.firstValue)
    }
}
