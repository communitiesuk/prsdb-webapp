package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_INCOMPLETE_PROPERTIES_PAGE
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
    private lateinit var incompletePropertyForLandlordService: IncompletePropertyForLandlordService

    @Test
    fun `addIncompletePropertyToLandlord adds a new entry to the LandlordIncompleteProperties join table`() {
        val landlordPrsdbUserId = "user-123"
        val landlordPrsdbUser = MockLandlordData.createPrsdbUser(id = landlordPrsdbUserId)
        val landlord = MockLandlordData.createLandlord(baseUser = landlordPrsdbUser)
        val savedJourneyState =
            MockSavedJourneyStateData.createSavedJourneyState(
                baseUser = landlordPrsdbUser,
            )
        whenever(landlordRepository.findByBaseUser_Id(landlordPrsdbUserId))
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

    @Test
    fun `getIncompletePropertiesForLandlord returns a page of incomplete properties data models`() {
        val principalName = "user-123"
        val prsdbUser = MockLandlordData.createPrsdbUser(id = principalName)
        val landlord = MockLandlordData.createLandlord(baseUser = prsdbUser)
        val savedJourneyState = MockSavedJourneyStateData.createSavedJourneyState(baseUser = prsdbUser)
        val lip = LandlordIncompleteProperties(landlord, savedJourneyState)
        val pageRequest =
            PageRequest.of(0, MAX_ENTRIES_IN_INCOMPLETE_PROPERTIES_PAGE, Sort.by("savedJourneyState.createdDate"))

        whenever(landlordIncompletePropertiesRepository.findByLandlord_BaseUser_Id(principalName, pageRequest))
            .thenReturn(PageImpl(listOf(lip), pageRequest, 1))

        val result = incompletePropertyForLandlordService.getIncompletePropertiesForLandlord(principalName, 0)

        assertEquals(1, result.totalElements)
        assertEquals(savedJourneyState.journeyId, result.content[0].journeyId)
    }
}
