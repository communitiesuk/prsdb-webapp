package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentCaptor.captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
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

    companion object {
        private val landlord = createLandlord()

        @JvmStatic
        fun provideRegNumsAndExpectedSearchResults() =
            listOf(
                Arguments.of(
                    Named.of(
                        "the registration number is valid",
                        RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber).toString(),
                    ),
                    listOf(LandlordSearchResultDataModel.fromLandlord(landlord)),
                ),
                Arguments.of(
                    Named.of(
                        "the registration number does not exist",
                        RegistrationNumberDataModel
                            .fromRegistrationNumber(
                                RegistrationNumber(RegistrationNumberType.LANDLORD, 1L),
                            ).toString(),
                    ),
                    emptyList<LandlordSearchResultDataModel>(),
                ),
                Arguments.of(
                    Named.of(
                        "the registration number is of the wrong type",
                        RegistrationNumberDataModel
                            .fromRegistrationNumber(RegistrationNumber(RegistrationNumberType.PROPERTY, 0L))
                            .toString(),
                    ),
                    emptyList<LandlordSearchResultDataModel>(),
                ),
                Arguments.of(
                    Named.of(
                        "the registration number is not a registration number",
                        "not a registration number",
                    ),
                    emptyList<LandlordSearchResultDataModel>(),
                ),
            )
    }

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

    @ParameterizedTest(name = "when {0}")
    @MethodSource("provideRegNumsAndExpectedSearchResults")
    fun `searchForLandlords returns a corresponding list of LandlordSearchResultDataModels (LRN query)`(
        registrationNumber: String,
        expectedSearchResults: List<LandlordSearchResultDataModel>,
    ) {
        whenever(mockLandlordRepository.findByRegistrationNumber_Number(landlord.registrationNumber.number))
            .thenReturn(landlord)

        whenever(mockLandlordRepository.searchMatching(eq(registrationNumber), any()))
            .thenReturn(PageImpl(emptyList<Landlord>()))

        val searchResults = landlordService.searchForLandlords(registrationNumber)

        assertEquals(expectedSearchResults, searchResults.content)
    }

    @Test
    fun `searchForLandlords returns a corresponding list of LandlordSearchResultDataModels`() {
        val searchQuery = "query"
        val maxEntriesOnPage = 25
        val pageRequest = PageRequest.of(0, maxEntriesOnPage)
        val matchingLandlords = listOf(createLandlord(), createLandlord(), createLandlord())
        val expectedSearchResults = matchingLandlords.map { LandlordSearchResultDataModel.fromLandlord(it) }

        whenever(mockLandlordRepository.searchMatching(searchQuery, pageRequest)).thenReturn(PageImpl(matchingLandlords))

        val searchResults = landlordService.searchForLandlords(searchQuery, 0, maxEntriesOnPage)

        assertEquals(expectedSearchResults, searchResults.content)
    }

    @Test
    fun `searchForLandlords returns the request page of LandlordSearchResultDataModels`() {
        val searchQuery = "query"
        val maxEntriesOnPage = 25

        val landlordsFromRepository = mutableListOf<Landlord>()
        for (i in 1..40) {
            landlordsFromRepository.add(createLandlord())
        }

        val pageRequest1 = PageRequest.of(0, maxEntriesOnPage)
        val pageRequest2 = PageRequest.of(1, maxEntriesOnPage)

        whenever(mockLandlordRepository.searchMatching(searchQuery, pageRequest1))
            .thenReturn(PageImpl(landlordsFromRepository.subList(0, maxEntriesOnPage)))
        whenever(mockLandlordRepository.searchMatching(searchQuery, pageRequest2))
            .thenReturn(PageImpl(landlordsFromRepository.subList(maxEntriesOnPage, 40)))

        val expectedSearchResultsPage1 =
            landlordsFromRepository
                .subList(0, maxEntriesOnPage)
                .map { LandlordSearchResultDataModel.fromLandlord(it) }
        val expectedSearchResultsPage2 =
            landlordsFromRepository
                .subList(maxEntriesOnPage, 40)
                .map { LandlordSearchResultDataModel.fromLandlord(it) }

        val searchResults1 = landlordService.searchForLandlords(searchQuery, 0, maxEntriesOnPage)
        val searchResults2 = landlordService.searchForLandlords(searchQuery, 1, maxEntriesOnPage)

        assertEquals(expectedSearchResultsPage1, searchResults1.content)
        assertEquals(expectedSearchResultsPage2, searchResults2.content)
    }
}
