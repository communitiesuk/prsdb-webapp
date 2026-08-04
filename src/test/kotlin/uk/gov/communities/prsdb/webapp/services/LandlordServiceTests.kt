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
import org.mockito.Mockito.verify
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.gov.communities.prsdb.webapp.constants.ENGLAND_OR_WALES
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlord
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.IndividualLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationLandlordRepository
import uk.gov.communities.prsdb.webapp.exceptions.RepositoryQueryTimeoutException
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LandlordSearchResultDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.LandlordUpdateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordUpdateConfirmation
import uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels.LandlordSearchResultViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createAddress
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createLandlordSearchResultDataModel
import java.net.URI
import java.time.LocalDate
import java.util.Optional
import kotlin.reflect.full.hasAnnotation
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class LandlordServiceTests {
    @Mock
    private lateinit var mockIndividualLandlordRepository: IndividualLandlordRepository

    @Mock
    private lateinit var mockOrganisationLandlordRepository: OrganisationLandlordRepository

    @Mock
    private lateinit var mockLandlordRepository: LandlordRepository

    @Mock
    private lateinit var mockUserToLandlordService: UserToLandlordService

    @Mock
    private lateinit var mockAddressService: AddressService

    @Mock
    private lateinit var mockRegistrationNumberService: RegistrationNumberService

    @Mock
    private lateinit var mockBackUrlStorageService: BackUrlStorageService

    @Mock
    private lateinit var updateConfirmationSender: EmailNotificationService<LandlordUpdateConfirmation>

    @Mock
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    private lateinit var landlordService: LandlordService

    @BeforeEach
    fun setup() {
        landlordService =
            LandlordService(
                mockIndividualLandlordRepository,
                mockOrganisationLandlordRepository,
                mockLandlordRepository,
                mockUserToLandlordService,
                mockAddressService,
                mockRegistrationNumberService,
                mockBackUrlStorageService,
                updateConfirmationSender,
                absoluteUrlProvider,
            )
    }

    @Test
    fun `retrieveLandlordById returns an individual landlord`() {
        val landlord = createIndividualLandlord()
        whenever(mockLandlordRepository.findById(landlord.id)).thenReturn(Optional.of(landlord))

        val result = landlordService.retrieveLandlordById(landlord.id)

        assertEquals(landlord, result)
    }

    @Test
    fun `retrieveLandlordById returns an organisation landlord`() {
        val landlord = OrganisationLandlord()
        whenever(mockLandlordRepository.findById(landlord.id)).thenReturn(Optional.of(landlord))

        val result = landlordService.retrieveLandlordById(landlord.id)

        assertEquals(landlord, result)
    }

    @Test
    fun `retrieveLandlordById returns null when landlord does not exist`() {
        whenever(mockLandlordRepository.findById(123L)).thenReturn(Optional.empty())

        val result = landlordService.retrieveLandlordById(123L)

        assertNull(result)
    }

    @Test
    fun `createIndividualLandlord creates a landlord and returns the landlord created`() {
        // Arrange
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")

        val baseUser = PrsdbUser("baseUserId")
        val address = Address(addressDataModel)
        val registrationNumber = RegistrationNumber(RegistrationNumberType.LANDLORD, 1233456)

        val expectedLandlord =
            IndividualLandlord(
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
                LocalDate.of(1990, 1, 1),
            )

        whenever(mockAddressService.findOrCreateAddress(addressDataModel)).thenReturn(address)
        whenever(mockRegistrationNumberService.createRegistrationNumber(RegistrationNumberType.LANDLORD)).thenReturn(
            registrationNumber,
        )
        whenever(mockIndividualLandlordRepository.save(any())).thenReturn(expectedLandlord)

        // Act
        val createdLandlord =
            landlordService.createIndividualLandlord(
                baseUser,
                "name",
                "example@email.com",
                "07123456789",
                addressDataModel,
                ENGLAND_OR_WALES,
                true,
                true,
                dateOfBirth = LocalDate.of(1990, 1, 1),
            )

        // Assert
        val landlordCaptor = captor<IndividualLandlord>()
        verify(mockIndividualLandlordRepository).save(landlordCaptor.capture())
        assertTrue(ReflectionEquals(expectedLandlord, "id").matches(landlordCaptor.value))

        assertEquals(expectedLandlord, createdLandlord)
    }

    @Nested
    inner class CreateOrganisationLandlordTests {
        private val orgAddressDataModel = AddressDataModel("1 Org Street, OG1 2AB", postcode = "OG1 2AB")
        private val trusteeAddressDataModel = AddressDataModel("2 Trustee Road, TR1 3CD", postcode = "TR1 3CD")

        private val orgAddress = Address(orgAddressDataModel)
        private val trusteeAddress = Address(trusteeAddressDataModel)
        private val registrationNumber = RegistrationNumber(RegistrationNumberType.LANDLORD, 9999999)

        @BeforeEach
        fun stubCommonDependencies() {
            whenever(mockAddressService.findOrCreateAddress(orgAddressDataModel)).thenReturn(orgAddress)
            whenever(mockRegistrationNumberService.createRegistrationNumber(RegistrationNumberType.LANDLORD))
                .thenReturn(registrationNumber)
            whenever(mockOrganisationLandlordRepository.save(any())).thenAnswer { it.arguments[0] }
        }

        private fun createOrganisationLandlord(
            leadTrusteeName: String? = null,
            leadTrusteeDateOfBirth: LocalDate? = null,
            leadTrusteeEmail: String? = null,
            leadTrusteePhoneNumber: String? = null,
            leadTrusteeAddress: AddressDataModel? = null,
        ): OrganisationLandlord =
            landlordService.createOrganisationLandlord(
                organisationName = "Test Org",
                organisationAddress = orgAddressDataModel,
                organisationEmail = "org@test.com",
                organisationPhoneNumber = "020 1234 5678",
                isCompany = true,
                isCharity = false,
                isTrust = false,
                companyNumber = "12345678",
                charityRegisteredWith = null,
                charityNumber = null,
                leadTrusteeName = leadTrusteeName,
                leadTrusteeDateOfBirth = leadTrusteeDateOfBirth,
                leadTrusteeEmail = leadTrusteeEmail,
                leadTrusteePhoneNumber = leadTrusteePhoneNumber,
                leadTrusteeAddress = leadTrusteeAddress,
                mainContactName = "Main Contact",
                mainContactEmail = "main@test.com",
                mainContactPhoneNumber = "071",
                registrantName = "Registrant",
                registrantDateOfBirth = LocalDate.of(1990, 1, 1),
                registrantEmail = "registrant@test.com",
                registrantPhoneNumber = "072",
            )

        @Test
        fun `creates an organisation landlord and returns it`() {
            val result = createOrganisationLandlord()

            val landlordCaptor = captor<OrganisationLandlord>()
            verify(mockOrganisationLandlordRepository).save(landlordCaptor.capture())

            val saved = landlordCaptor.value
            assertEquals("Test Org", saved.name)
            assertEquals(orgAddress, saved.address)
            assertEquals("org@test.com", saved.wholeOrgEmail)
            assertEquals("020 1234 5678", saved.phoneNumber)
            assertEquals(true, saved.isCompany)
            assertEquals(false, saved.isCharity)
            assertEquals(false, saved.isTrust)
            assertEquals("12345678", saved.companyNumber)
            assertEquals("Main Contact", saved.mainContactName)
            assertEquals("main@test.com", saved.mainContactEmail)
            assertEquals("071", saved.mainContactPhone)
            assertEquals("Registrant", saved.registrantName)
            assertEquals(LocalDate.of(1990, 1, 1), saved.registrantDateOfBirth)
            assertEquals("registrant@test.com", saved.registrantEmail)
            assertEquals("072", saved.registrantPhoneNumber)
            assertEquals(registrationNumber, saved.registrationNumber)
            assertEquals(result, saved)
        }

        @Test
        fun `resolves lead trustee address when provided`() {
            whenever(mockAddressService.findOrCreateAddress(trusteeAddressDataModel)).thenReturn(trusteeAddress)

            createOrganisationLandlord(
                leadTrusteeName = "Jane Trustee",
                leadTrusteeDateOfBirth = LocalDate.of(1980, 6, 15),
                leadTrusteeEmail = "trustee@test.com",
                leadTrusteePhoneNumber = "07999",
                leadTrusteeAddress = trusteeAddressDataModel,
            )

            verify(mockAddressService, times(2)).findOrCreateAddress(any())

            val landlordCaptor = captor<OrganisationLandlord>()
            verify(mockOrganisationLandlordRepository).save(landlordCaptor.capture())
            assertEquals(trusteeAddress, landlordCaptor.value.leadTrusteeAddress)
        }

        @Test
        fun `does not resolve lead trustee address when null`() {
            createOrganisationLandlord(leadTrusteeAddress = null)

            verify(mockAddressService, times(1)).findOrCreateAddress(any())

            val landlordCaptor = captor<OrganisationLandlord>()
            verify(mockOrganisationLandlordRepository).save(landlordCaptor.capture())
            assertNull(landlordCaptor.value.leadTrusteeAddress)
        }
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
            whenever(mockIndividualLandlordRepository.searchMatching(searchTerm, lcUserBaseId, pageable = pageRequest))
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
            whenever(mockIndividualLandlordRepository.searchMatchingLRN(searchLRN, lcUserBaseId, pageable = pageRequest))
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

            whenever(mockIndividualLandlordRepository.searchMatching(searchTerm, lcUserBaseId, pageable = pageRequest))
                .thenReturn(Page.empty())

            // Act
            val searchResults =
                landlordService.searchForLandlords(searchTerm, lcUserBaseId, requestedPageIndex = requestedPageNumber, pageSize = pageSize)

            // Assert
            val expectedSearchResults = emptyList<LandlordSearchResultViewModel>()
            assertEquals(expectedSearchResults, searchResults.content)
            verify(mockIndividualLandlordRepository, never()).searchMatchingLRN(any(), any(), any(), any())
        }

        @Test
        fun `searchForLandlords returns no results when given a searchTerm that has no LRN or fuzzy search matches`() {
            // Arrange
            val searchTerm = "non-matching searchTerm"
            val lcUserBaseId = "lcUserBaseId"
            val requestedPageNumber = 0
            val pageSize = 25
            val pageRequest = PageRequest.of(requestedPageNumber, pageSize)

            whenever(mockIndividualLandlordRepository.searchMatching(searchTerm, lcUserBaseId, pageable = pageRequest))
                .thenReturn(Page.empty())

            // Act
            val searchResults =
                landlordService.searchForLandlords(searchTerm, lcUserBaseId, requestedPageIndex = requestedPageNumber, pageSize = pageSize)

            // Assert
            val expectedSearchResults = emptyList<LandlordSearchResultViewModel>()
            assertEquals(expectedSearchResults, searchResults.content)
            verify(mockIndividualLandlordRepository, never()).searchMatchingLRN(any(), any(), any(), any())
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
            whenever(mockIndividualLandlordRepository.searchMatching(searchTerm, lcUserBaseId, pageable = pageRequest1))
                .thenReturn(PageImpl(matchingLandlordsPage1))

            val pageNumber2 = 1
            val pageRequest2 = PageRequest.of(pageNumber2, pageSize)
            val matchingLandlordsPage2 = matchingLandlords.subList(pageSize, matchingLandlords.size)
            whenever(mockIndividualLandlordRepository.searchMatching(searchTerm, lcUserBaseId, pageable = pageRequest2))
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

            whenever(mockIndividualLandlordRepository.searchMatching(searchTerm, lcUserBaseId, pageable = pageRequest))
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
        val originalName = "original name"
        val originalEmail = "original email"
        val originalPhoneNumber = "original phone number"
        val originalDateOfBirth = LocalDate.of(1991, 1, 1)
        val landlordEntity =
            createIndividualLandlord(
                name = originalName,
                email = originalEmail,
                phoneNumber = originalPhoneNumber,
                dateOfBirth = originalDateOfBirth,
            )
        val updateModel = LandlordUpdateModel(null, null, null, null, null)

        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlordEntity)

        // Act
        landlordService.updateLandlordForUser(updateModel) {}

        // Assert
        assertEquals(originalName, landlordEntity.name)
        assertEquals(originalEmail, landlordEntity.email)
        assertEquals(originalPhoneNumber, landlordEntity.phoneNumber)
        assertEquals(originalDateOfBirth, landlordEntity.dateOfBirth)
    }

    @Test
    fun `when update landlord is passed an update model, non-null fields provided are applied to the entity`() {
        // Arrange
        val landlordEntity =
            createIndividualLandlord(
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
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlordEntity)
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("example.com/landlord-dashboard"))

        // Act
        landlordService.updateLandlordForUser(updateModel) {}

        // Assert
        assertEquals(updateModel.name, landlordEntity.name)
        assertEquals(updateModel.email, landlordEntity.email)
        assertEquals(updateModel.phoneNumber, landlordEntity.phoneNumber)
        assertEquals(newAddress, landlordEntity.address)
        assertEquals(updateModel.dateOfBirth, landlordEntity.dateOfBirth)
    }

    @Test
    fun `updateLandlordAddress applies the new address to the entity`() {
        // Arrange
        val landlordEntity = createIndividualLandlord(address = createAddress("original address"))
        val newAddress = createAddress("new address")
        val newAddressDataModel = AddressDataModel.fromAddress(newAddress)

        whenever(mockAddressService.findOrCreateAddress(newAddressDataModel)).thenReturn(newAddress)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlordEntity)
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("example.com/landlord-dashboard"))

        // Act
        landlordService.updateLandlordAddress(newAddressDataModel)

        // Assert
        assertEquals(newAddress, landlordEntity.address)
    }

    @ParameterizedTest
    @MethodSource("getUpdateAndExpectedEmailPairs")
    fun `when a landlord is updated, a corresponding email is sent to each relevant email`(
        updateModel: LandlordUpdateModel,
        expectedDetail: String,
    ) {
        // Arrange
        val originalEmailAddress = "original email"
        val landlordEntity =
            createIndividualLandlord(
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
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlordEntity)
        val dashboardUrl = URI("example.com/landlord-dashboard")
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUrl)

        // Act
        landlordService.updateLandlordForUser(updateModel) {}

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
    fun `when a landlord updates their email by case only, a single confirmation email is sent and the new casing is stored`() {
        // Arrange
        val originalEmailAddress = "landlord@example.com"
        val newCasingEmailAddress = "Landlord@Example.com"
        val landlordEntity =
            createIndividualLandlord(
                name = "original name",
                email = originalEmailAddress,
                phoneNumber = "original phone number",
                address = createAddress("original address"),
                dateOfBirth = LocalDate.of(1991, 1, 1),
            )
        val updateModel = LandlordUpdateModel(newCasingEmailAddress, null, null, null, null)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlordEntity)
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("example.com/landlord-dashboard"))

        // Act
        val updatedLandlord = landlordService.updateLandlordForUser(updateModel) {}

        // Assert
        assertEquals(newCasingEmailAddress, (updatedLandlord as IndividualLandlord).email)
        verify(updateConfirmationSender, times(1)).sendEmail(eq(newCasingEmailAddress), any())
        verify(updateConfirmationSender, times(1)).sendEmail(any(), any())
    }

    @Test
    fun `when checkUpdateIsValid throws an exception, no update occurs`() {
        // Arrange
        val originalName = "original name"
        val originalEmail = "original email"
        val originalPhoneNumber = "original phone number"
        val originalDateOfBirth = LocalDate.of(1991, 1, 1)
        val landlordEntity =
            createIndividualLandlord(
                name = originalName,
                email = originalEmail,
                phoneNumber = originalPhoneNumber,
                dateOfBirth = originalDateOfBirth,
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

        // Act
        try {
            landlordService.updateLandlordForUser(updateModel) { throw Exception("Invalid update") }
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
    fun `updateLandlordForUser is annotated with @Transactional`() {
        assertTrue(landlordService::updateLandlordForUser.hasAnnotation<Transactional>())
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
