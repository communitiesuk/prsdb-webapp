package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_INCOMPLETE_PROPERTIES_PAGE
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperties
import uk.gov.communities.prsdb.webapp.database.repository.IncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class UsersIncompletePropertyServiceTests {
    @Mock
    private lateinit var savedJourneyStateRepository: SavedJourneyStateRepository

    @Mock
    private lateinit var incompletePropertiesRepository: IncompletePropertiesRepository

    @InjectMocks
    private lateinit var usersIncompletePropertyService: UsersIncompletePropertyService

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

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
    fun `getCurrentUsersIncompleteProperties returns a page of incomplete properties data models`() {
        val principalName = "user-123"
        val prsdbUser = MockLandlordData.createPrsdbUser(id = principalName)
        val savedJourneyState = MockSavedJourneyStateData.createSavedJourneyState(baseUser = prsdbUser)
        val lip = LandlordIncompleteProperties(prsdbUser, savedJourneyState)
        val pageRequest =
            PageRequest.of(0, MAX_ENTRIES_IN_INCOMPLETE_PROPERTIES_PAGE, Sort.by("savedJourneyState.createdDate"))
        setMockPrincipal(principalName)

        whenever(incompletePropertiesRepository.findByUser_Id(principalName, pageRequest))
            .thenReturn(PageImpl(listOf(lip), pageRequest, 1))

        val result = usersIncompletePropertyService.getCurrentUsersIncompleteProperties(0)

        assertEquals(1, result.totalElements)
        assertEquals(savedJourneyState.journeyId, result.content[0].journeyId)
    }

    @Test
    fun `getCurrentUsersIncompleteProperties queries the repository by the logged in user's id, not an org-wide id`() {
        val requestingUserId = "org-user-1"
        val prsdbUser = MockLandlordData.createPrsdbUser(id = requestingUserId)
        val savedJourneyState = MockSavedJourneyStateData.createSavedJourneyState(baseUser = prsdbUser)
        val lip = LandlordIncompleteProperties(prsdbUser, savedJourneyState)
        val pageRequest =
            PageRequest.of(0, MAX_ENTRIES_IN_INCOMPLETE_PROPERTIES_PAGE, Sort.by("savedJourneyState.createdDate"))
        setMockPrincipal(requestingUserId)

        whenever(incompletePropertiesRepository.findByUser_Id(requestingUserId, pageRequest))
            .thenReturn(PageImpl(listOf(lip), pageRequest, 1))

        val result = usersIncompletePropertyService.getCurrentUsersIncompleteProperties(0)

        assertEquals(1, result.totalElements)
        verify(incompletePropertiesRepository).findByUser_Id(requestingUserId, pageRequest)
    }

    @Test
    fun `getCurrentUsersIncompletePropertiesCount returns the count from the repository for the logged in user`() {
        val userId = "user-123"
        setMockPrincipal(userId)
        whenever(incompletePropertiesRepository.countByUser_Id(userId)).thenReturn(3L)

        val result = usersIncompletePropertyService.getCurrentUsersIncompletePropertiesCount()

        assertEquals(3, result)
    }

    private fun setMockPrincipal(name: String) {
        val authentication = mock<Authentication>()
        whenever(authentication.name).thenReturn(name)
        val context = mock<SecurityContext>()
        whenever(context.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(context)
    }
}
