package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperties
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class IncompletePropertyForLandlordServiceTests {
    @Mock
    private lateinit var savedJourneyStateRepository: SavedJourneyStateRepository

    @Mock
    private lateinit var landlordRepository: LandlordRepository

    @Mock
    private lateinit var landlordIncompletePropertiesRepository: LandlordIncompletePropertiesRepository

    @InjectMocks
    private lateinit var incompletePropertyForLandlordService: IncompletePropertyForLandlordServiceImpl

    @Test
    fun `addIncompletePropertyToLandlord adds a new entry to the LandlordIncompleteProperties join table`() {
        val landlordOneLoginId = "user-123"
        val landlordOneLoginUser = MockLandlordData.createOneLoginUser(id = landlordOneLoginId)
        val landlord = MockLandlordData.createLandlord(baseUser = landlordOneLoginUser)
        val savedJourneyState =
            MockSavedJourneyStateData.createSavedJourneyState(
                baseUser = landlordOneLoginUser,
            )
        whenever(landlordRepository.findByBaseUser_Id(landlordOneLoginId))
            .thenReturn(landlord)
        val expectedNewEntry = LandlordIncompleteProperties(landlord, savedJourneyState)

        val captor = argumentCaptor<LandlordIncompleteProperties>()

        // Act
        incompletePropertyForLandlordService.addIncompletePropertyToLandlord(savedJourneyState)

        // Assert
        verify(landlordIncompletePropertiesRepository).save(captor.capture())

        val savedEntry = captor.firstValue
        assertEquals(expectedNewEntry.landlord, savedEntry.landlord)
        assertEquals(expectedNewEntry.savedJourneyState, savedEntry.savedJourneyState)
    }
}
