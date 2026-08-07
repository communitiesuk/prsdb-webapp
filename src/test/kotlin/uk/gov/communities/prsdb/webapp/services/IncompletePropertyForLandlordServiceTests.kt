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
import uk.gov.communities.prsdb.webapp.database.repository.IncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class IncompletePropertyForLandlordServiceTests {
    @Mock
    private lateinit var savedJourneyStateRepository: SavedJourneyStateRepository

    @Mock
    private lateinit var incompletePropertiesRepository: IncompletePropertiesRepository

    @InjectMocks
    private lateinit var usersIncompletePropertyService: UsersIncompletePropertyService

    @Test
    fun `addIncompletePropertyForUser adds a new entry to the LandlordIncompleteProperties join table keyed on the journey's user`() {
        val userId = "user-123"
        val prsdbUser = MockLandlordData.createPrsdbUser(id = userId)
        val savedJourneyState = MockSavedJourneyStateData.createSavedJourneyState(baseUser = prsdbUser)
        val expectedNewEntry = LandlordIncompleteProperties(prsdbUser, savedJourneyState)

        val captor = argumentCaptor<LandlordIncompleteProperties>()

        // Act
        usersIncompletePropertyService.addIncompletePropertyForUser(savedJourneyState)

        // Assert
        verify(incompletePropertiesRepository).save(captor.capture())

        val savedEntry = captor.firstValue
        assertEquals(expectedNewEntry.user, savedEntry.user)
        assertEquals(expectedNewEntry.savedJourneyState, savedEntry.savedJourneyState)
    }

    @Test
    fun `getIncompletePropertiesForLandlord returns a page of CurrentUsersIncomplete properties data models  user`() {
        val principalName = "user-123"
        val prsdbUser = MockLandlordData.createPrsdbUser(id = principalName)
        val savedJourneyState = MockSavedJourneyStateData.createSavedJourneyState(baseUser = prsdbUser)
        val lip = LandlordIncompleteProperties(prsdbUser, savedJourneyState)
        val pageRequest =
            PageRequest.of(0, MAX_ENTRIES_IN_INCOMPLETE_PROPERTIES_PAGE, Sort.by("savedJourneyState.createdDate"))

        whenever(incompletePropertiesRepository.findByUser_Id(principalName, pageRequest))
            .thenReturn(PageImpl(listOf(lip), pageRequest, 1))

        val result = usersIncompletePropertyService.getCurrentUsersIncompleteProperties(principalName, 0)

        assertEquals(1, result.totalElements)
        assertEquals(savedJourneyState.journeyId, result.content[0].journeyId)
    }

    @Test
    fun `getIncompletePropertiesForLandlord returns only the CurrentUsersIncomplete properties  user, not their colleagues'`() {
        val requestingUserId = "org-user-1"
        val prsdbUser = MockLandlordData.createPrsdbUser(id = requestingUserId)
        val savedJourneyState = MockSavedJourneyStateData.createSavedJourneyState(baseUser = prsdbUser)
        val lip = LandlordIncompleteProperties(prsdbUser, savedJourneyState)
        val pageRequest =
            PageRequest.of(0, MAX_ENTRIES_IN_INCOMPLETE_PROPERTIES_PAGE, Sort.by("savedJourneyState.createdDate"))

        // The repository is mocked to only return this user's own entry - this test documents/locks in
        // that the service passes through the requesting user's id (not an org-wide id) to the repository.
        whenever(incompletePropertiesRepository.findByUser_Id(requestingUserId, pageRequest))
            .thenReturn(PageImpl(listOf(lip), pageRequest, 1))

        val result = usersIncompletePropertyService.getCurrentUsersIncompleteProperties(requestingUserId, 0)

        assertEquals(1, result.totalElements)
        verify(incompletePropertiesRepository).findByUser_Id(requestingUserId, pageRequest)
    }

    @Test
    fun `getNumberOfIncompletePropertiesForUser returns the count from the repository for the given user`() {
        val userId = "user-123"
        whenever(incompletePropertiesRepository.countByUser_Id(userId)).thenReturn(3L)

        val result = usersIncompletePropertyService.getCurrentUsersIncompletePropertiesCount(userId)

        assertEquals(3, result)
    }
}
