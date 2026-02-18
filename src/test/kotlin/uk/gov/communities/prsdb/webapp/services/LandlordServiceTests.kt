package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentCaptor.captor
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.gov.communities.prsdb.webapp.constants.ENGLAND_OR_WALES
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.exceptions.RepositoryQueryTimeoutException
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LandlordSearchResultDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.LandlordUpdateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordUpdateConfirmation
import uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels.LandlordSearchResultViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createAddress
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createLandlordSearchResultDataModel
import java.net.URI
import java.time.LocalDate
import kotlin.reflect.full.hasAnnotation
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class LandlordServiceTests {
    @Mock
    private lateinit var mockLandlordRepository: LandlordRepository

    @Mock
    private lateinit var mockOneLoginUserService: OneLoginUserService

    @Mock
    private lateinit var mockAddressService: AddressService

    @Mock
    private lateinit var mockRegistrationNumberService: RegistrationNumberService

    @Mock
    private lateinit var mockBackUrlStorageService: BackUrlStorageService

    @Mock
    private lateinit var updateConfirmationSender: EmailNotificationService<LandlordUpdateConfirmation>

    @Mock
    private lateinit var registrationConfirmationSender: EmailNotificationService<LandlordRegistrationConfirmationEmail>

    @Mock
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    private lateinit var landlordService: LandlordService

    // Need to inject mocks manually as "injectMocks" gets confused between the two EmailNotificationServices
    @BeforeEach
    fun setup() {
        landlordService =
            LandlordService(
                mockLandlordRepository,
                mockOneLoginUserService,
                mockAddressService,
                mockRegistrationNumberService,
                mockBackUrlStorageService,
                updateConfirmationSender,
                absoluteUrlProvider,
                registrationConfirmationSender,
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
    fun `createLandlord creates a landlord and returns the landlord created`() {
        // Arrange
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
                ENGLAND_OR_WALES,
                true,
                true,
                null,
                null,
            )

        whenever(mockOneLoginUserService.findOrCreate1LUser(baseUserId)).thenReturn(baseUser)
        whenever(mockAddressService.findOrCreateAddress(addressDataModel)).thenReturn(address)
        whenever(mockRegistrationNumberService.createRegistrationNumber(RegistrationNumberType.LANDLORD)).thenReturn(
            registrationNumber,
        )
        whenever(mockLandlordRepository.save(any())).thenReturn(expectedLandlord)
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("example.com"))

        // Act
        val createdLandlord =
            landlordService.createLandlord(
                baseUserId,
                "name",
                "example@email.com",
                "07123456789",
                addressDataModel,
                ENGLAND_OR_WALES,
                true,
                true,
            )

        // Assert
        val landlordCaptor = captor<Landlord>()
        verify(mockLandlordRepository).save(landlordCaptor.capture())
        assertTrue(ReflectionEquals(expectedLandlord, "id").matches(landlordCaptor.value))

        assertEquals(expectedLandlord, createdLandlord)
    }

    @Test
    fun `createLandlord sends a confirmation email for the landlord created`() {
        // Arrange
        val expectedLandlord = createLandlord()

        whenever(mockOneLoginUserService.findOrCreate1LUser(any())).thenReturn(expectedLandlord.baseUser)
        whenever(mockAddressService.findOrCreateAddress(any())).thenReturn(expectedLandlord.address)
        whenever(mockRegistrationNumberService.createRegistrationNumber(any()))
            .thenReturn(expectedLandlord.registrationNumber)

        whenever(mockLandlordRepository.save(any())).thenReturn(expectedLandlord)
        val dashboardUri = URI("example.com/dashboard")
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)

        // Act
        landlordService.createLandlord(
            "baseUserId",
            "name",
            "example@email.com",
            "07123456789",
            mock(),
            ENGLAND_OR_WALES,
            true,
            true,
        )

        // Assert
        verify(registrationConfirmationSender).sendEmail(
            expectedLandlord.email,
            LandlordRegistrationConfirmationEmail(
                RegistrationNumberDataModel.fromRegistrationNumber(expectedLandlord.registrationNumber).toString(),
                dashboardUri.toASCIIString(),
            ),
        )
    }

    @Nested
    inner class SearchForLandlordsTests {
        @Test
        fun `searchForLandlords returns a corresponding list of LandlordSearchResultViewModels`() {
            // Arrange
            val searchTerm = "searchTerm"
            val lcUserBaseId = "lcUserBaseId"
            val requestedPageNumber = 0
            val pageSize = 25
            val pageRequest = PageRequest.of(requestedPageNumber, pageSize)

            val matchingLandlords =
                listOf(createLandlordSearchResultDataModel(), createLandlordSearchResultDataModel(), createLandlordSearchResultDataModel())
            whenever(mockLandlordRepository.searchMatching(searchTerm, lcUserBaseId, pageable = pageRequest))
                .thenReturn(PageImpl(matchingLandlords))

            val currentUrlKey = 77
            whenever(mockBackUrlStorageService.storeCurrentUrlReturningKey()).thenReturn(currentUrlKey)

            // Act
            val searchResults =
                landlordService.searchForLandlords(searchTerm, lcUserBaseId, requestedPageIndex = requestedPageNumber, pageSize = pageSize)

            // Assert
            val expectedSearchResults =
                matchingLandlords.map {
                    LandlordSearchResultViewModel.fromDataModel(
                        it,
                        currentUrlKey,
                    )
                }
            assertEquals(expectedSearchResults, searchResults.content)
        }

        @Test
        fun `searchForLandlords returns a corresponding list of LandlordSearchResultViewModels (LRN searchTerm)`() {
            // Arrange
            val searchTerm = "L-CCCC-CCCC"
            val searchLRN = RegistrationNumberDataModel.parseTypeOrNull(searchTerm, RegistrationNumberType.LANDLORD)!!.number
            val lcUserBaseId = "lcUserBaseId"
            val requestedPageNumber = 0
            val pageSize = 25
            val pageRequest = PageRequest.of(requestedPageNumber, pageSize)

            val matchingLandlord = listOf(createLandlordSearchResultDataModel())
            whenever(mockLandlordRepository.searchMatchingLRN(searchLRN, lcUserBaseId, pageable = pageRequest))
                .thenReturn(PageImpl(matchingLandlord))

            val currentUrlKey = 79
            whenever(mockBackUrlStorageService.storeCurrentUrlReturningKey()).thenReturn(currentUrlKey)

            // Act
            val searchResults =
                landlordService.searchForLandlords(searchTerm, lcUserBaseId, requestedPageIndex = requestedPageNumber, pageSize = pageSize)

            // Assert
            val expectedSearchResults =
                matchingLandlord.map {
                    LandlordSearchResultViewModel.fromDataModel(
                        it,
                        currentUrlKey,
                    )
                }
            assertEquals(expectedSearchResults, searchResults.content)
        }

        @Test
        fun `searchForLandlords returns no results when given a non-landlord registration number`() {
            // Arrange
            val searchTerm = "P-CCCC-CCCC"
            val lcUserBaseId = "lcUserBaseId"
            val requestedPageNumber = 0
            val pageSize = 25
            val pageRequest = PageRequest.of(requestedPageNumber, pageSize)

            whenever(mockLandlordRepository.searchMatching(searchTerm, lcUserBaseId, pageable = pageRequest))
                .thenReturn(Page.empty())

            // Act
            val searchResults =
                landlordService.searchForLandlords(searchTerm, lcUserBaseId, requestedPageIndex = requestedPageNumber, pageSize = pageSize)

            // Assert
            val expectedSearchResults = emptyList<LandlordSearchResultViewModel>()
            assertEquals(expectedSearchResults, searchResults.content)
            verify(mockLandlordRepository, never()).searchMatchingLRN(any(), any(), any(), any())
        }

        @Test
        fun `searchForLandlords returns no results when given a searchTerm that has no LRN or fuzzy search matches`() {
            // Arrange
            val searchTerm = "non-matching searchTerm"
            val lcUserBaseId = "lcUserBaseId"
            val requestedPageNumber = 0
            val pageSize = 25
            val pageRequest = PageRequest.of(requestedPageNumber, pageSize)

            whenever(mockLandlordRepository.searchMatching(searchTerm, lcUserBaseId, pageable = pageRequest))
                .thenReturn(Page.empty())

            // Act
            val searchResults =
                landlordService.searchForLandlords(searchTerm, lcUserBaseId, requestedPageIndex = requestedPageNumber, pageSize = pageSize)

            // Assert
            val expectedSearchResults = emptyList<LandlordSearchResultViewModel>()
            assertEquals(expectedSearchResults, searchResults.content)
            verify(mockLandlordRepository, never()).searchMatchingLRN(any(), any(), any(), any())
        }

        @Test
        fun `searchForLandlords returns the requested page of LandlordSearchResultViewModels`() {
            // Arrange
            val searchTerm = "searchTerm"
            val lcUserBaseId = "lcUserBaseId"
            val pageSize = 25

            val matchingLandlords =
                mutableListOf<LandlordSearchResultDataModel>().apply {
                    for (i in 1..40) {
                        add(createLandlordSearchResultDataModel())
                    }
                }

            val pageNumber1 = 0
            val pageRequest1 = PageRequest.of(pageNumber1, pageSize)
            val matchingLandlordsPage1 = matchingLandlords.subList(0, pageSize)
            whenever(mockLandlordRepository.searchMatching(searchTerm, lcUserBaseId, pageable = pageRequest1))
                .thenReturn(PageImpl(matchingLandlordsPage1))

            val pageNumber2 = 1
            val pageRequest2 = PageRequest.of(pageNumber2, pageSize)
            val matchingLandlordsPage2 = matchingLandlords.subList(pageSize, matchingLandlords.size)
            whenever(mockLandlordRepository.searchMatching(searchTerm, lcUserBaseId, pageable = pageRequest2))
                .thenReturn(PageImpl(matchingLandlordsPage2))

            val currentUrlKey = 77
            whenever(mockBackUrlStorageService.storeCurrentUrlReturningKey()).thenReturn(currentUrlKey).thenReturn(currentUrlKey)

            // Act
            val searchResults1 =
                landlordService.searchForLandlords(searchTerm, lcUserBaseId, requestedPageIndex = pageNumber1, pageSize = pageSize)

            val searchResults2 =
                landlordService.searchForLandlords(searchTerm, lcUserBaseId, requestedPageIndex = pageNumber2, pageSize = pageSize)

            // Assert
            val expectedSearchResultsPage1 =
                matchingLandlordsPage1.map {
                    LandlordSearchResultViewModel.fromDataModel(it, currentUrlKey)
                }
            assertEquals(expectedSearchResultsPage1, searchResults1.content)

            val expectedSearchResultsPage2 =
                matchingLandlordsPage2.map {
                    LandlordSearchResultViewModel.fromDataModel(it, currentUrlKey)
                }
            assertEquals(expectedSearchResultsPage2, searchResults2.content)
        }

        @Test
        fun `searchForLandlords throws an exception when fuzzy searching times out`() {
            // Arrange
            val searchTerm = "searchTerm"
            val lcUserBaseId = "lcUserBaseId"
            val requestedPageNumber = 0
            val pageSize = 25
            val pageRequest = PageRequest.of(requestedPageNumber, pageSize)

            whenever(mockLandlordRepository.searchMatching(searchTerm, lcUserBaseId, pageable = pageRequest))
                .thenThrow(QueryTimeoutException("Query timed out"))

            // Act & Assert
            assertThrows<RepositoryQueryTimeoutException> {
                landlordService.searchForLandlords(searchTerm, lcUserBaseId, requestedPageIndex = requestedPageNumber, pageSize = pageSize)
            }
        }
    }

    @Test
    fun `when update landlord is passed an update model, null fields provided do not change the entity`() {
        // Arrange
        val userId = "my id"
        val originalName = "original name"
        val originalEmail = "original email"
        val originalPhoneNumber = "original phone number"
        val originalDateOfBirth = LocalDate.of(1991, 1, 1)
        val landlordEntity =
            createLandlord(name = originalName, email = originalEmail, phoneNumber = originalPhoneNumber, dateOfBirth = originalDateOfBirth)
        val updateModel = LandlordUpdateModel(null, null, null, null, null)

        whenever(mockLandlordRepository.findByBaseUser_Id(userId)).thenReturn(landlordEntity)

        // Act
        landlordService.updateLandlordForBaseUserId(userId, updateModel) {}

        // Assert
        assertEquals(originalName, landlordEntity.name)
        assertEquals(originalEmail, landlordEntity.email)
        assertEquals(originalPhoneNumber, landlordEntity.phoneNumber)
        assertEquals(originalDateOfBirth, landlordEntity.dateOfBirth)
    }

    @Test
    fun `when update landlord is passed an update model, non-null fields provided are applied to the entity`() {
        // Arrange
        val userId = "my id"
        val landlordEntity =
            createLandlord(
                name = "original name",
                email = "original email",
                phoneNumber = "original phone number",
                address = createAddress("original address"),
                dateOfBirth = LocalDate.of(1991, 1, 1),
            )
        val newAddress = createAddress("new address")
        val updateModel =
            LandlordUpdateModel(
                "newEmail",
                "newName",
                "new phone number",
                AddressDataModel.fromAddress(newAddress),
                LocalDate.of(1992, 2, 2),
            )

        whenever(mockAddressService.findOrCreateAddress(updateModel.address!!)).thenReturn(newAddress)
        whenever(mockLandlordRepository.findByBaseUser_Id(userId)).thenReturn(landlordEntity)
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("example.com/landlord-dashboard"))

        // Act
        landlordService.updateLandlordForBaseUserId(userId, updateModel) {}

        // Assert
        assertEquals(updateModel.name, landlordEntity.name)
        assertEquals(updateModel.email, landlordEntity.email)
        assertEquals(updateModel.phoneNumber, landlordEntity.phoneNumber)
        assertEquals(newAddress, landlordEntity.address)
        assertEquals(updateModel.dateOfBirth, landlordEntity.dateOfBirth)
    }

    @ParameterizedTest
    @MethodSource("getUpdateAndExpectedEmailPairs")
    fun `when a landlord is updated, a corresponding email is sent to each relevant email`(
        updateModel: LandlordUpdateModel,
        expectedDetail: String,
    ) {
        // Arrange
        val originalEmailAddress = "original email"
        val userId = "my id"
        val landlordEntity =
            createLandlord(
                name = "original name",
                email = originalEmailAddress,
                phoneNumber = "original phone number",
                address = createAddress("original address"),
                dateOfBirth = LocalDate.of(1991, 1, 1),
            )
        updateModel.address?.let {
            val address = Address(updateModel.address)
            whenever(mockAddressService.findOrCreateAddress(it)).thenReturn(address)
        }
        whenever(mockLandlordRepository.findByBaseUser_Id(userId)).thenReturn(landlordEntity)
        val dashboardUrl = URI("example.com/landlord-dashboard")
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUrl)

        // Act
        landlordService.updateLandlordForBaseUserId(userId, updateModel) {}

        // Assert
        val expectedEmailModel =
            LandlordUpdateConfirmation(
                RegistrationNumberDataModel.fromRegistrationNumber(landlordEntity.registrationNumber).toString(),
                dashboardUrl,
                expectedDetail,
            )

        verify(updateConfirmationSender).sendEmail(
            eq(originalEmailAddress),
            eq(expectedEmailModel),
        )

        updateModel.email?.let {
            verify(updateConfirmationSender).sendEmail(
                eq(it),
                eq(expectedEmailModel),
            )
        }
    }

    @Test
    fun `when checkUpdateIsValid throws an exception, no update occurs`() {
        // Arrange
        val userId = "my id"
        val originalName = "original name"
        val originalEmail = "original email"
        val originalPhoneNumber = "original phone number"
        val originalDateOfBirth = LocalDate.of(1991, 1, 1)
        val landlordEntity =
            createLandlord(name = originalName, email = originalEmail, phoneNumber = originalPhoneNumber, dateOfBirth = originalDateOfBirth)
        val newAddress = createAddress("new address")
        val updateModel =
            LandlordUpdateModel(
                "newEmail",
                "newName",
                "new phone number",
                AddressDataModel.fromAddress(newAddress),
                LocalDate.of(1992, 2, 2),
            )

        // Act
        try {
            landlordService.updateLandlordForBaseUserId(userId, updateModel) { throw Exception("Invalid update") }
        } catch (_: Exception) {
            // Expected exception, do nothing
        }

        // Assert
        assertEquals(originalName, landlordEntity.name)
        assertEquals(originalEmail, landlordEntity.email)
        assertEquals(originalPhoneNumber, landlordEntity.phoneNumber)
        assertEquals(originalDateOfBirth, landlordEntity.dateOfBirth)
    }

    @Test
    fun `updateLandlordForBaseUserId is annotated with @Transactional`() {
        assertTrue(landlordService::updateLandlordForBaseUserId.hasAnnotation<Transactional>())
    }

    companion object {
        @JvmStatic
        fun getUpdateAndExpectedEmailPairs() =
            listOf(
                Arguments.of(
                    LandlordUpdateModel(
                        "newEmail",
                        null,
                        null,
                        null,
                        null,
                    ),
                    "email address",
                ),
                Arguments.of(
                    LandlordUpdateModel(
                        null,
                        "newName",
                        null,
                        null,
                        null,
                    ),
                    "name",
                ),
                Arguments.of(
                    LandlordUpdateModel(
                        null,
                        null,
                        "new phone number",
                        null,
                        null,
                    ),
                    "telephone number",
                ),
                Arguments.of(
                    LandlordUpdateModel(
                        null,
                        null,
                        null,
                        AddressDataModel.fromAddress(createAddress("new address")),
                        null,
                    ),
                    "contact address",
                ),
                Arguments.of(
                    LandlordUpdateModel(
                        null,
                        null,
                        null,
                        null,
                        LocalDate.of(1922, 2, 2),
                    ),
                    "date of birth",
                ),
            )
    }
}
