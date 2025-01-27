package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.LandlordWithListedPropertyCount
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordWithListedPropertyCountRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData.Companion.createLandlord
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LandlordSearchResultDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LandlordServiceTests {
    @Mock
    private lateinit var mockLandlordRepository: LandlordRepository

    @Mock
    private lateinit var mockOneLoginUserRepository: OneLoginUserRepository

    @Mock
    private lateinit var mockLandlordWithListedPropertyCountRepository: LandlordWithListedPropertyCountRepository

    @Mock
    private lateinit var mockAddressService: AddressService

    @Mock
    private lateinit var mockRegistrationNumberService: RegistrationNumberService

    @InjectMocks
    private lateinit var landlordService: LandlordService

    @Test
    fun `retrieveLandlordByRegNum returns a landlord given its registration number`() {
        val regNumDataModel = RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, 0L)
        val expectedLandlord = Landlord()

        whenever(mockLandlordRepository.findByRegistrationNumber_Number(regNumDataModel.number)).thenReturn(
            expectedLandlord,
        )

        val landlord = landlordService.retrieveLandlordByRegNum(regNumDataModel)

        assertEquals(expectedLandlord, landlord)
    }

    @Test
    fun `retrieveLandlordByRegNum returns a null given a non-existent landlord registration number`() {
        assertNull(
            landlordService.retrieveLandlordByRegNum(
                RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, 0L),
            ),
        )
    }

    @Test
    fun `retrieveLandlordByRegNum throws an illegal argument exception when given a non-landlord registration number`() {
        assertThrows<IllegalArgumentException> {
            landlordService.retrieveLandlordByRegNum(
                RegistrationNumberDataModel(RegistrationNumberType.PROPERTY, 0L),
            )
        }
    }

    @Test
    fun `retrieveLandlordByBaseUserId returns a landlord given its base user ID`() {
        val baseUserId = "baseUserId"
        val expectedLandlord = Landlord()

        whenever(mockLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(expectedLandlord)

        val landlord = landlordService.retrieveLandlordByBaseUserId(baseUserId)

        assertEquals(expectedLandlord, landlord)
    }

    @Test
    fun `retrieveLandlordByBaseUserId returns a null given an unregistered base user ID`() {
        assertNull(
            landlordService.retrieveLandlordByBaseUserId(
                "unregisteredBaseUserId",
            ),
        )
    }

    @Test
    fun `createLandlord creates a landlord`() {
        val baseUserId = "baseUserId"
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")

        val baseUser = OneLoginUser(baseUserId)
        val address = Address(addressDataModel)
        val registrationNumber = RegistrationNumber(RegistrationNumberType.LANDLORD, 1233456)

        val expectedLandlord =
            Landlord(
                baseUser,
                "name",
                "example@email.com",
                "07123456789",
                address,
                registrationNumber,
                null,
                null,
            )

        whenever(mockOneLoginUserRepository.getReferenceById(baseUserId)).thenReturn(baseUser)
        whenever(mockAddressService.findOrCreateAddress(addressDataModel)).thenReturn(address)
        whenever(mockRegistrationNumberService.createRegistrationNumber(RegistrationNumberType.LANDLORD)).thenReturn(
            registrationNumber,
        )

        landlordService.createLandlord(
            baseUserId,
            "name",
            "example@email.com",
            "07123456789",
            addressDataModel,
        )

        val landlordCaptor = captor<Landlord>()
        verify(mockLandlordRepository).save(landlordCaptor.capture())
        assertTrue(ReflectionEquals(expectedLandlord, "id").matches(landlordCaptor.value))
    }

    @Test
    fun `searchForLandlords returns a corresponding list of LandlordSearchResultDataModels`() {
        val searchQuery = "query"
        val laUserBaseId = "laUserBaseId"
        val currentPageNumber = 0
        val pageSize = 25
        val pageRequest = PageRequest.of(currentPageNumber, pageSize)

        val matchingLandlords = mutableListOf<Landlord>()
        val matchingLandlordsWithListedPropertyCount = mutableListOf<LandlordWithListedPropertyCount>()
        for (i in 1..3) {
            val landlord = createLandlord()
            matchingLandlords.add(landlord)
            matchingLandlordsWithListedPropertyCount.add(LandlordWithListedPropertyCount(landlord.id, landlord, 0))
        }

        val expectedFormattedSearchResults =
            matchingLandlordsWithListedPropertyCount.map { LandlordSearchResultDataModel.fromLandlordWithListedPropertyCount(it) }

        whenever(
            mockLandlordRepository.searchMatching(searchQuery, laUserBaseId, pageable = pageRequest),
        ).thenReturn(PageImpl(matchingLandlords))
        whenever(mockLandlordWithListedPropertyCountRepository.findByLandlordIdIn(matchingLandlords.map { it.id }))
            .thenReturn(matchingLandlordsWithListedPropertyCount)

        val searchResults =
            landlordService.searchForLandlords(
                searchQuery,
                laUserBaseId,
                currentPageNumber = currentPageNumber,
                pageSize = pageSize,
            )

        assertEquals(expectedFormattedSearchResults, searchResults.content)
    }

    @Test
    fun `searchForLandlords returns a corresponding list of LandlordSearchResultDataModels (LRN query)`() {
        val searchQuery = "L-CCCC-CCCC"
        val searchLRN =
            RegistrationNumberDataModel.parseTypeOrNull(searchQuery, RegistrationNumberType.LANDLORD)!!.number
        val laUserBaseId = "laUserBaseId"
        val currentPageNumber = 0
        val pageSize = 25
        val pageRequest = PageRequest.of(currentPageNumber, pageSize)
        val matchingLandlord = listOf(createLandlord())
        val matchingLandlordWithListedPropertyCount =
            listOf(LandlordWithListedPropertyCount(matchingLandlord[0].id, matchingLandlord[0], 0))
        val expectedFormattedSearchResults =
            matchingLandlordWithListedPropertyCount.map { LandlordSearchResultDataModel.fromLandlordWithListedPropertyCount(it) }

        whenever(
            mockLandlordRepository.searchMatchingLRN(searchLRN, laUserBaseId, pageable = pageRequest),
        ).thenReturn(PageImpl(matchingLandlord))
        whenever(mockLandlordWithListedPropertyCountRepository.findByLandlordIdIn(listOf(matchingLandlord[0].id)))
            .thenReturn(matchingLandlordWithListedPropertyCount)

        val searchResults =
            landlordService.searchForLandlords(
                searchQuery,
                laUserBaseId,
                currentPageNumber = currentPageNumber,
                pageSize = pageSize,
            )

        assertEquals(expectedFormattedSearchResults, searchResults.content)
    }

    @Test
    fun `searchForLandlords returns no results when given a non-landlord registration number`() {
        val searchQuery = "P-CCCC-CCCC"
        val laUserBaseId = "laUserBaseId"
        val currentPageNumber = 0
        val pageSize = 25
        val expectedPageRequest = PageRequest.of(currentPageNumber, pageSize)
        val expectedSearchResults = emptyList<LandlordSearchResultDataModel>()

        whenever(
            mockLandlordRepository.searchMatching(searchQuery, laUserBaseId, pageable = expectedPageRequest),
        ).thenReturn(Page.empty())

        val searchResults =
            landlordService.searchForLandlords(
                searchQuery,
                laUserBaseId,
                currentPageNumber = currentPageNumber,
                pageSize = pageSize,
            )

        verify(mockLandlordRepository, never()).searchMatchingLRN(any(), any(), any(), any())
        assertEquals(expectedSearchResults, searchResults.content)
    }

    @Test
    fun `searchForLandlords returns no results when given a query that has no LRN or fuzzy search matches`() {
        val searchQuery = "non-matching query"
        val laUserBaseId = "laUserBaseId"
        val currentPageNumber = 0
        val pageSize = 25
        val expectedPageRequest = PageRequest.of(currentPageNumber, pageSize)
        val expectedSearchResults = emptyList<LandlordSearchResultDataModel>()

        whenever(
            mockLandlordRepository.searchMatching(searchQuery, laUserBaseId, pageable = expectedPageRequest),
        ).thenReturn(Page.empty())

        val searchResults =
            landlordService.searchForLandlords(
                searchQuery,
                laUserBaseId,
                currentPageNumber = currentPageNumber,
                pageSize = pageSize,
            )

        verify(mockLandlordRepository, never()).searchMatchingLRN(any(), any(), any(), any())
        assertEquals(expectedSearchResults, searchResults.content)
    }

    @Test
    fun `searchForLandlords returns the requested page of LandlordSearchResultDataModels`() {
        val searchQuery = "query"
        val laUserBaseId = "laUserBaseId"
        val pageSize = 25

        val matchingLandlords = mutableListOf<Landlord>()
        val matchingLandlordsWithListedPropertyCount = mutableListOf<LandlordWithListedPropertyCount>()
        for (i in 1..40) {
            val landlord = createLandlord()
            matchingLandlords.add(landlord)
            matchingLandlordsWithListedPropertyCount.add(LandlordWithListedPropertyCount(landlord.id, landlord, i.mod(3)))
        }

        val pageNumber1 = 0
        val pageRequest1 = PageRequest.of(pageNumber1, pageSize)
        val matchingLandlordsPage1 = matchingLandlords.subList(0, pageSize)
        val matchingLandlordsWithListedPropertiesPage1 = matchingLandlordsWithListedPropertyCount.subList(0, pageSize)
        val expectedFormattedSearchResultsPage1 =
            matchingLandlordsWithListedPropertiesPage1.map { LandlordSearchResultDataModel.fromLandlordWithListedPropertyCount(it) }

        val pageNumber2 = 1
        val pageRequest2 = PageRequest.of(pageNumber2, pageSize)
        val matchingLandlordsPage2 = matchingLandlords.subList(pageSize, 40)
        val matchingLandlordsWithListedPropertiesPage2 = matchingLandlordsWithListedPropertyCount.subList(pageSize, 40)
        val expectedFormattedSearchResultsPage2 =
            matchingLandlordsWithListedPropertiesPage2.map { LandlordSearchResultDataModel.fromLandlordWithListedPropertyCount(it) }

        whenever(mockLandlordRepository.searchMatching(searchQuery, laUserBaseId, pageable = pageRequest1))
            .thenReturn(PageImpl(matchingLandlordsPage1))
        whenever(mockLandlordRepository.searchMatching(searchQuery, laUserBaseId, pageable = pageRequest2))
            .thenReturn(PageImpl(matchingLandlordsPage2))
        whenever(mockLandlordWithListedPropertyCountRepository.findByLandlordIdIn(matchingLandlordsPage1.map { it.id }))
            .thenReturn(matchingLandlordsWithListedPropertiesPage1)
        whenever(mockLandlordWithListedPropertyCountRepository.findByLandlordIdIn(matchingLandlordsPage2.map { it.id }))
            .thenReturn(matchingLandlordsWithListedPropertiesPage2)

        val searchResults1 =
            landlordService.searchForLandlords(
                searchQuery,
                laUserBaseId,
                currentPageNumber = pageNumber1,
                pageSize = pageSize,
            )
        val searchResults2 =
            landlordService.searchForLandlords(
                searchQuery,
                laUserBaseId,
                currentPageNumber = pageNumber2,
                pageSize = pageSize,
            )

        assertEquals(expectedFormattedSearchResultsPage1, searchResults1.content)
        assertEquals(expectedFormattedSearchResultsPage2, searchResults2.content)
    }
}
