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
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
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
        val expectedPageRequest = PageRequest.of(currentPageNumber, pageSize)
        val expectedSearchResults = listOf(createLandlord(), createLandlord())
        val expectedFormattedSearchResults =
            expectedSearchResults.map { LandlordSearchResultDataModel.fromLandlord(it) }

        whenever(
            mockLandlordRepository.searchMatching(searchQuery, laUserBaseId, pageable = expectedPageRequest),
        ).thenReturn(PageImpl(expectedSearchResults))

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
        val expectedPageRequest = PageRequest.of(currentPageNumber, pageSize)
        val expectedSearchResults = listOf(createLandlord(), createLandlord())
        val expectedFormattedSearchResults =
            expectedSearchResults.map { LandlordSearchResultDataModel.fromLandlord(it) }

        whenever(
            mockLandlordRepository.searchMatchingLRN(searchLRN, laUserBaseId, pageable = expectedPageRequest),
        ).thenReturn(PageImpl(expectedSearchResults))

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
    fun `searchForLandlords returns the requested page of LandlordSearchResultDataModels`() {
        val searchQuery = "query"
        val laUserBaseId = "laUserBaseId"
        val pageSize = 25

        val landlordsFromRepository = mutableListOf<Landlord>()
        for (i in 1..40) {
            landlordsFromRepository.add(createLandlord())
        }

        val pageNumber1 = 0
        val expectedPageRequest1 = PageRequest.of(pageNumber1, pageSize)
        val expectedSearchResults1 = landlordsFromRepository.subList(0, pageSize)
        val expectedFormattedSearchResults1 =
            expectedSearchResults1.map { LandlordSearchResultDataModel.fromLandlord(it) }

        val pageNumber2 = 1
        val expectedPageRequest2 = PageRequest.of(pageNumber2, pageSize)
        val expectedSearchResults2 = landlordsFromRepository.subList(pageSize, 40)
        val expectedFormattedSearchResults2 =
            expectedSearchResults2.map { LandlordSearchResultDataModel.fromLandlord(it) }

        whenever(mockLandlordRepository.searchMatching(searchQuery, laUserBaseId, pageable = expectedPageRequest1))
            .thenReturn(PageImpl(expectedSearchResults1))
        whenever(mockLandlordRepository.searchMatching(searchQuery, laUserBaseId, pageable = expectedPageRequest2))
            .thenReturn(PageImpl(expectedSearchResults2))

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

        assertEquals(expectedFormattedSearchResults1, searchResults1.content)
        assertEquals(expectedFormattedSearchResults2, searchResults2.content)
    }
}
