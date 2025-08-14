package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentCaptor.captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor.Companion.overrideBackLinkForUrl
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OccupancyType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationStatus
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.Property
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyOwnershipUpdateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyUpdateConfirmation
import uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels.PropertySearchResultViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.RegisteredPropertyViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockOneLoginUserData
import java.net.URI

@ExtendWith(MockitoExtension::class)
class PropertyOwnershipServiceTests {
    @Mock
    private lateinit var mockPropertyOwnershipRepository: PropertyOwnershipRepository

    @Mock
    private lateinit var mockRegistrationNumberService: RegistrationNumberService

    @Mock
    private lateinit var mockLocalAuthorityDataService: LocalAuthorityDataService

    @Mock
    private lateinit var mockLicenseService: LicenseService

    @Mock
    private lateinit var mockFormContextService: FormContextService

    @Mock
    private lateinit var mockBackUrlStorageService: BackUrlStorageService

    @Mock
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var emailNotificationService: EmailNotificationService<PropertyUpdateConfirmation>

    @InjectMocks
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @Test
    fun `createPropertyOwnership creates a property ownership`() {
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)
        val ownershipType = OwnershipType.FREEHOLD
        val households = 1
        val tenants = 2
        val landlord = MockLandlordData.createLandlord()
        val property = Property()
        val license = License()
        val incompleteComplianceForm = FormContext(JourneyType.PROPERTY_COMPLIANCE, landlord.baseUser)

        val expectedPropertyOwnership =
            PropertyOwnership(
                occupancyType = OccupancyType.SINGLE_FAMILY_DWELLING,
                ownershipType = ownershipType,
                currentNumHouseholds = households,
                currentNumTenants = tenants,
                registrationNumber = registrationNumber,
                primaryLandlord = landlord,
                property = property,
                license = license,
                incompleteComplianceForm = incompleteComplianceForm,
            )

        whenever(mockRegistrationNumberService.createRegistrationNumber(RegistrationNumberType.PROPERTY)).thenReturn(
            registrationNumber,
        )
        whenever(mockFormContextService.createEmptyFormContext(JourneyType.PROPERTY_COMPLIANCE, landlord.baseUser)).thenReturn(
            incompleteComplianceForm,
        )
        whenever(mockPropertyOwnershipRepository.save(any<PropertyOwnership>())).thenReturn(
            expectedPropertyOwnership,
        )

        propertyOwnershipService.createPropertyOwnership(
            ownershipType,
            households,
            tenants,
            landlord,
            property,
            license,
        )

        val propertyOwnershipCaptor = captor<PropertyOwnership>()
        verify(mockPropertyOwnershipRepository).save(propertyOwnershipCaptor.capture())
        assertTrue(ReflectionEquals(expectedPropertyOwnership).matches(propertyOwnershipCaptor.value))
    }

    @Test
    fun `createPropertyOwnership can create a property ownership with no license`() {
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)
        val ownershipType = OwnershipType.FREEHOLD
        val households = 1
        val tenants = 2
        val landlord = MockLandlordData.createLandlord()
        val property = Property()
        val incompleteComplianceForm = FormContext(JourneyType.PROPERTY_COMPLIANCE, landlord.baseUser)

        val expectedPropertyOwnership =
            PropertyOwnership(
                occupancyType = OccupancyType.SINGLE_FAMILY_DWELLING,
                ownershipType = ownershipType,
                currentNumHouseholds = households,
                currentNumTenants = tenants,
                registrationNumber = registrationNumber,
                primaryLandlord = landlord,
                property = property,
                license = null,
                incompleteComplianceForm = incompleteComplianceForm,
            )

        whenever(mockRegistrationNumberService.createRegistrationNumber(RegistrationNumberType.PROPERTY)).thenReturn(
            registrationNumber,
        )
        whenever(mockFormContextService.createEmptyFormContext(JourneyType.PROPERTY_COMPLIANCE, landlord.baseUser)).thenReturn(
            incompleteComplianceForm,
        )
        whenever(mockPropertyOwnershipRepository.save(any<PropertyOwnership>())).thenReturn(
            expectedPropertyOwnership,
        )

        propertyOwnershipService.createPropertyOwnership(
            ownershipType,
            households,
            tenants,
            landlord,
            property,
        )

        val propertyOwnershipCaptor = captor<PropertyOwnership>()
        verify(mockPropertyOwnershipRepository).save(propertyOwnershipCaptor.capture())
        assertTrue(ReflectionEquals(expectedPropertyOwnership).matches(propertyOwnershipCaptor.value))
    }

    @Nested
    inner class GetLandlordRegisteredPropertiesDetails {
        private val landlord = MockLandlordData.createLandlord()
        private val localAuthority = LocalAuthority(11, "DERBYSHIRE DALES DISTRICT COUNCIL", "1045")
        private val expectedPropertyLicence = "forms.checkPropertyAnswers.propertyDetails.noLicensing"
        private val expectedIsTenantedMessageKey = "commonText.no"
        private val expectedCurrentUrlKey = 101

        private val propertyOwnership1 =
            MockLandlordData.createPropertyOwnership(
                primaryLandlord = landlord,
                property =
                    MockLandlordData.createProperty(
                        address = MockLandlordData.createAddress("11 Example Road, EG1 2AB", localAuthority),
                    ),
                registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456),
                license = null,
                currentNumTenants = 0,
            )

        private val propertyOwnership2 =
            MockLandlordData.createPropertyOwnership(
                primaryLandlord = landlord,
                property =
                    MockLandlordData.createProperty(
                        address = MockLandlordData.createAddress("12 Example Road, EG1 2AB", localAuthority),
                    ),
                registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 654321),
                license = null,
                currentNumTenants = 0,
            )

        private val landlordsProperties: List<PropertyOwnership> = listOf(propertyOwnership1, propertyOwnership2)

        @Test
        fun `Returns a list of Landlords properties in correctly formatted data model from landlords BaseUser_Id`() {
            whenever(
                mockPropertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndProperty_Status(
                    "landlord",
                    RegistrationStatus.REGISTERED,
                ),
            ).thenReturn(landlordsProperties)

            whenever(mockBackUrlStorageService.storeCurrentUrlReturningKey()).thenReturn(expectedCurrentUrlKey)

            val expectedResults: List<RegisteredPropertyViewModel> =
                listOf(
                    RegisteredPropertyViewModel(
                        address = propertyOwnership1.property.address.singleLineAddress,
                        registrationNumber =
                            RegistrationNumberDataModel
                                .fromRegistrationNumber(propertyOwnership1.registrationNumber)
                                .toString(),
                        localAuthorityName = localAuthority.name,
                        licenseTypeMessageKey = expectedPropertyLicence,
                        isTenantedMessageKey = expectedIsTenantedMessageKey,
                        recordLink =
                            PropertyDetailsController
                                .getPropertyDetailsPath(propertyOwnership1.id)
                                .overrideBackLinkForUrl(expectedCurrentUrlKey),
                    ),
                    RegisteredPropertyViewModel(
                        address = propertyOwnership2.property.address.singleLineAddress,
                        registrationNumber =
                            RegistrationNumberDataModel
                                .fromRegistrationNumber(propertyOwnership2.registrationNumber)
                                .toString(),
                        localAuthorityName = localAuthority.name,
                        licenseTypeMessageKey = expectedPropertyLicence,
                        isTenantedMessageKey = expectedIsTenantedMessageKey,
                        recordLink =
                            PropertyDetailsController
                                .getPropertyDetailsPath(propertyOwnership2.id)
                                .overrideBackLinkForUrl(expectedCurrentUrlKey),
                    ),
                )

            val result = propertyOwnershipService.getRegisteredPropertiesForLandlordUser("landlord")

            assertTrue(result.size == 2)
            assertEquals(expectedResults, result)
        }

        @Test
        fun `Returns a list of Landlords properties in correctly formatted data model from landlords Id`() {
            whenever(
                mockPropertyOwnershipRepository.findAllByPrimaryLandlord_IdAndIsActiveTrueAndProperty_Status(
                    landlord.id,
                    RegistrationStatus.REGISTERED,
                ),
            ).thenReturn(landlordsProperties)

            whenever(mockBackUrlStorageService.storeCurrentUrlReturningKey()).thenReturn(expectedCurrentUrlKey)

            val expectedResults: List<RegisteredPropertyViewModel> =
                listOf(
                    RegisteredPropertyViewModel(
                        address = propertyOwnership1.property.address.singleLineAddress,
                        registrationNumber =
                            RegistrationNumberDataModel
                                .fromRegistrationNumber(propertyOwnership1.registrationNumber)
                                .toString(),
                        localAuthorityName = localAuthority.name,
                        licenseTypeMessageKey = expectedPropertyLicence,
                        isTenantedMessageKey = expectedIsTenantedMessageKey,
                        recordLink =
                            PropertyDetailsController
                                .getPropertyDetailsPath(propertyOwnership1.id, isLaView = true)
                                .overrideBackLinkForUrl(expectedCurrentUrlKey),
                    ),
                    RegisteredPropertyViewModel(
                        address = propertyOwnership2.property.address.singleLineAddress,
                        registrationNumber =
                            RegistrationNumberDataModel
                                .fromRegistrationNumber(propertyOwnership2.registrationNumber)
                                .toString(),
                        localAuthorityName = localAuthority.name,
                        licenseTypeMessageKey = expectedPropertyLicence,
                        isTenantedMessageKey = expectedIsTenantedMessageKey,
                        recordLink =
                            PropertyDetailsController
                                .getPropertyDetailsPath(propertyOwnership2.id, isLaView = true)
                                .overrideBackLinkForUrl(expectedCurrentUrlKey),
                    ),
                )

            val result = propertyOwnershipService.getRegisteredPropertiesForLandlord(landlord.id)

            assertTrue(result.size == 2)
            assertEquals(expectedResults, result)
        }
    }

    @Nested
    inner class GetPropertyOwnershipIfAuthorizedUser {
        @Test
        fun `throws not found error if an active property ownership does not exist`() {
            val invalidId: Long = 1
            val principalName = "landlord"
            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(invalidId)).thenReturn(null)

            val errorThrown =
                assertThrows<ResponseStatusException> {
                    propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(invalidId, principalName)
                }
            assertEquals(HttpStatus.NOT_FOUND, errorThrown.statusCode)
        }

        @Test
        fun `throws not found error if user is not primary landlord or an la user`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership()
            val principalName = "not-the-landlord"
            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
                propertyOwnership,
            )

            val errorThrown =
                assertThrows<ResponseStatusException> {
                    propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(propertyOwnership.id, principalName)
                }
            assertEquals(HttpStatus.NOT_FOUND, errorThrown.statusCode)
        }

        @Test
        fun `returns property ownership when user is an la user`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership()
            val localAuthorityUser =
                MockLocalAuthorityData.createLocalAuthorityUser(
                    MockOneLoginUserData.createOneLoginUser("not-the-landlord"),
                    MockLocalAuthorityData.createLocalAuthority(),
                )
            val principalName = localAuthorityUser.baseUser.id

            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
                propertyOwnership,
            )

            whenever(mockLocalAuthorityDataService.getIsLocalAuthorityUser(principalName)).thenReturn(true)

            val result =
                propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(propertyOwnership.id, principalName)

            assertEquals(result, propertyOwnership)
        }

        @Test
        fun `returns property ownership when user is primary landlord`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership()
            val principalName = propertyOwnership.primaryLandlord.baseUser.id

            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
                propertyOwnership,
            )

            whenever(mockLocalAuthorityDataService.getIsLocalAuthorityUser(principalName)).thenReturn(false)

            val result =
                propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(propertyOwnership.id, principalName)

            assertEquals(result, propertyOwnership)
        }
    }

    @Nested
    inner class GetIsAuthorizedToEditRecord {
        @Test
        fun `returns true when the user is the property's primary landlord`() {
            val baseUserId = "baseUserId"
            val propertyOwnership =
                MockLandlordData.createPropertyOwnership(
                    primaryLandlord =
                        MockLandlordData.createLandlord(
                            baseUser = MockLandlordData.createOneLoginUser(baseUserId),
                        ),
                )

            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(propertyOwnership)

            val returnedIsPrimaryLandlord = propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnership.id, baseUserId)

            assertTrue(returnedIsPrimaryLandlord)
        }

        @Test
        fun `returns false when the user is not the property's primary landlord`() {
            val propertyOwnership =
                MockLandlordData.createPropertyOwnership(
                    primaryLandlord =
                        MockLandlordData.createLandlord(
                            baseUser = MockLandlordData.createOneLoginUser("baseUserId"),
                        ),
                )

            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(propertyOwnership)

            val returnedIsPrimaryLandlord =
                propertyOwnershipService.getIsAuthorizedToEditRecord(
                    propertyOwnership.id,
                    "differentBaseUserId",
                )

            assertFalse(returnedIsPrimaryLandlord)
        }

        @Test
        fun `throws not found error if the property ownership does not exist`() {
            val errorThrown =
                assertThrows<ResponseStatusException> {
                    propertyOwnershipService.getIsAuthorizedToEditRecord(1, "anyBaseUserId")
                }
            assertEquals(HttpStatus.NOT_FOUND, errorThrown.statusCode)
        }
    }

    @Test
    fun `searchForProperties returns a single matching property when the search term is a PRN`() {
        val searchPRN = RegistrationNumberDataModel(RegistrationNumberType.PROPERTY, 123)
        val laBaseUserId = "id"
        val pageRequest = PageRequest.of(1, 10)
        val prnMatchingPropertyOwnership = listOf(MockLandlordData.createPropertyOwnership())
        val currentUrlKey = 13
        val expectedSearchResults =
            prnMatchingPropertyOwnership.map { PropertySearchResultViewModel.fromPropertyOwnership(it, currentUrlKey) }

        whenever(
            mockPropertyOwnershipRepository.searchMatchingPRN(searchPRN.number, laBaseUserId, pageable = pageRequest),
        ).thenReturn(
            PageImpl(prnMatchingPropertyOwnership),
        )

        whenever(mockBackUrlStorageService.storeCurrentUrlReturningKey()).thenReturn(currentUrlKey)

        val searchResults =
            propertyOwnershipService.searchForProperties(
                searchPRN.toString(),
                laBaseUserId,
                requestedPageIndex = pageRequest.pageNumber,
                pageSize = pageRequest.pageSize,
            )

        assertEquals(expectedSearchResults, searchResults.content)
    }

    @Test
    fun `searchForProperties returns no results when the search term is a non-property registration number`() {
        val nonPropertyRegNum = RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, 123)
        val laBaseUserId = "id"
        val pageRequest = PageRequest.of(1, 10)

        whenever(
            mockPropertyOwnershipRepository.searchMatching(
                nonPropertyRegNum.toString(),
                laBaseUserId,
                pageable = pageRequest,
            ),
        ).thenReturn(
            Page.empty(),
        )

        val searchResults =
            propertyOwnershipService.searchForProperties(
                nonPropertyRegNum.toString(),
                laBaseUserId,
                requestedPageIndex = pageRequest.pageNumber,
                pageSize = pageRequest.pageSize,
            )

        verify(mockPropertyOwnershipRepository, never()).searchMatchingPRN(
            nonPropertyRegNum.number,
            laBaseUserId,
            pageable = pageRequest,
        )
        assertEquals(emptyList<PropertySearchResultViewModel>(), searchResults.content)
    }

    @Test
    fun `searchForProperties returns a single matching property when the search term is a UPRN`() {
        val searchUPRN = "123"
        val laBaseUserId = "id"
        val pageRequest = PageRequest.of(1, 10)
        val currentUrlKey = 23

        val uprnMatchingPropertyOwnership =
            listOf(
                MockLandlordData.createPropertyOwnership(
                    property =
                        MockLandlordData.createProperty(
                            address =
                                MockLandlordData.createAddress(
                                    uprn = searchUPRN.toLong(),
                                ),
                        ),
                ),
            )
        val expectedSearchResults =
            uprnMatchingPropertyOwnership.map { PropertySearchResultViewModel.fromPropertyOwnership(it, currentUrlKey) }

        whenever(
            mockPropertyOwnershipRepository.searchMatchingUPRN(
                searchUPRN.toLong(),
                laBaseUserId,
                pageable = pageRequest,
            ),
        ).thenReturn(
            PageImpl(uprnMatchingPropertyOwnership),
        )
        whenever(mockBackUrlStorageService.storeCurrentUrlReturningKey()).thenReturn(currentUrlKey)

        val searchResults =
            propertyOwnershipService.searchForProperties(
                searchUPRN,
                laBaseUserId,
                requestedPageIndex = pageRequest.pageNumber,
                pageSize = pageRequest.pageSize,
            )

        assertEquals(expectedSearchResults, searchResults.content)
    }

    @Test
    fun `searchForProperties returns a collection of fuzzy matches when the search term is not a PRN or UPRN`() {
        val searchTerm = "EG1 2AB"
        val laBaseUserId = "id"
        val urlKey = 7
        val pageRequest = PageRequest.of(1, 10)

        val fuzzyMatchingPropertyOwnerships =
            listOf(MockLandlordData.createPropertyOwnership(), MockLandlordData.createPropertyOwnership())
        val expectedSearchResults =
            fuzzyMatchingPropertyOwnerships.map { PropertySearchResultViewModel.fromPropertyOwnership(it, 7) }

        whenever(
            mockPropertyOwnershipRepository.searchMatching(searchTerm, laBaseUserId, pageable = pageRequest),
        ).thenReturn(
            PageImpl(fuzzyMatchingPropertyOwnerships),
        )
        whenever(mockBackUrlStorageService.storeCurrentUrlReturningKey()).thenReturn(urlKey)

        val searchResults =
            propertyOwnershipService.searchForProperties(
                searchTerm,
                laBaseUserId,
                requestedPageIndex = pageRequest.pageNumber,
                pageSize = pageRequest.pageSize,
            )

        assertEquals(expectedSearchResults, searchResults.content)
    }

    @Test
    fun `searchForProperties returns the requested page of properties`() {
        val searchTerm = "searchTerm"
        val laBaseUserId = "id"
        val pageSize = 25
        val matchingProperties = (1..40).map { MockLandlordData.createPropertyOwnership() }

        val pageIndex1 = 0
        val urlKey1 = 37
        val pageRequest1 = PageRequest.of(pageIndex1, pageSize)
        val matchingPropertiesPage1 = matchingProperties.subList(0, pageSize)
        val expectedPage1SearchResults =
            matchingPropertiesPage1.map { PropertySearchResultViewModel.fromPropertyOwnership(it, urlKey1) }

        val pageIndex2 = 1
        val urlKey2 = 41
        val pageRequest2 = PageRequest.of(pageIndex2, pageSize)
        val matchingPropertiesPage2 = matchingProperties.subList(pageSize, matchingProperties.size)
        val expectedPage2SearchResults =
            matchingPropertiesPage2.map { PropertySearchResultViewModel.fromPropertyOwnership(it, urlKey2) }

        whenever(mockPropertyOwnershipRepository.searchMatching(searchTerm, laBaseUserId, pageable = pageRequest1))
            .thenReturn(PageImpl(matchingPropertiesPage1))
        whenever(mockPropertyOwnershipRepository.searchMatching(searchTerm, laBaseUserId, pageable = pageRequest2))
            .thenReturn(PageImpl(matchingPropertiesPage2))

        whenever(mockBackUrlStorageService.storeCurrentUrlReturningKey()).thenReturn(urlKey1)
        val searchResults1 =
            propertyOwnershipService.searchForProperties(searchTerm, laBaseUserId, requestedPageIndex = pageIndex1)

        whenever(mockBackUrlStorageService.storeCurrentUrlReturningKey()).thenReturn(urlKey2)
        val searchResults2 =
            propertyOwnershipService.searchForProperties(searchTerm, laBaseUserId, requestedPageIndex = pageIndex2)

        assertEquals(expectedPage1SearchResults, searchResults1.content)
        assertEquals(expectedPage2SearchResults, searchResults2.content)
    }

    @Test
    fun `updatePropertyOwnership does not change the fields associated with the given update model's null values`() {
        // Arrange
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = 1,
                ownershipType = OwnershipType.FREEHOLD,
                currentNumHouseholds = 2,
                currentNumTenants = 4,
                license = License(LicensingType.SELECTIVE_LICENCE, "licenceNumber"),
            )
        val originalOwnershipType = propertyOwnership.ownershipType
        val originalNumberOfHouseholds = propertyOwnership.currentNumHouseholds
        val originalNumberOfPeople = propertyOwnership.currentNumTenants
        val originalLicence = propertyOwnership.license!!
        val updateModel =
            PropertyOwnershipUpdateModel(null, null, null, null, null)

        whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
            propertyOwnership,
        )

        // Act
        propertyOwnershipService.updatePropertyOwnership(propertyOwnership.id, updateModel) {}

        // Assert
        assertEquals(originalOwnershipType, propertyOwnership.ownershipType)
        assertEquals(originalNumberOfHouseholds, propertyOwnership.currentNumHouseholds)
        assertEquals(originalNumberOfPeople, propertyOwnership.currentNumTenants)
        assertEquals(originalLicence, propertyOwnership.license)
        verify(mockLicenseService, never()).updateLicence(any(), any(), any())
    }

    @Test
    fun `updatePropertyOwnership does not send a confirmation email when no fields are changed`() {
        // Arrange
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = 1,
                ownershipType = OwnershipType.FREEHOLD,
                currentNumHouseholds = 2,
                currentNumTenants = 4,
                license = License(LicensingType.SELECTIVE_LICENCE, "licenceNumber"),
            )
        val updateModel =
            PropertyOwnershipUpdateModel(
                propertyOwnership.ownershipType,
                propertyOwnership.currentNumHouseholds,
                propertyOwnership.currentNumTenants,
                null,
                null,
            )

        whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
            propertyOwnership,
        )

        // Act
        propertyOwnershipService.updatePropertyOwnership(propertyOwnership.id, updateModel) {}

        // Assert
        verify(emailNotificationService, never()).sendEmail(
            any(),
            any(),
        )
    }

    @Test
    fun `updatePropertyOwnership changes the fields associated with the given update model's non-null values`() {
        // Arrange
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = 1,
                ownershipType = OwnershipType.FREEHOLD,
                currentNumHouseholds = 2,
                currentNumTenants = 6,
                license = License(LicensingType.SELECTIVE_LICENCE, "licenceNumberSelective"),
            )

        val updateLicence = License(LicensingType.HMO_MANDATORY_LICENCE, "licenceNumberMandatory")
        val updateModel =
            PropertyOwnershipUpdateModel(
                ownershipType = OwnershipType.LEASEHOLD,
                numberOfHouseholds = 1,
                numberOfPeople = 2,
                licensingType = updateLicence.licenseType,
                licenceNumber = updateLicence.licenseNumber,
            )

        whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
            propertyOwnership,
        )
        whenever(
            mockLicenseService.updateLicence(propertyOwnership.license, updateModel.licensingType, updateModel.licenceNumber),
        ).thenReturn(updateLicence)

        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://example.com"))

        // Act
        propertyOwnershipService.updatePropertyOwnership(propertyOwnership.id, updateModel) {}

        // Assert
        assertEquals(updateModel.ownershipType, propertyOwnership.ownershipType)
        assertEquals(updateModel.numberOfHouseholds, propertyOwnership.currentNumHouseholds)
        assertEquals(updateModel.numberOfPeople, propertyOwnership.currentNumTenants)
        assertEquals(updateModel.licensingType, propertyOwnership.license?.licenseType)
        assertEquals(updateModel.licenceNumber, propertyOwnership.license?.licenseNumber)
    }

    @MethodSource("updatesAndConfirmationEmails")
    @ParameterizedTest
    fun `updatePropertyOwnership sends a matching confirmation email when updating a property ownership`(
        propertyOwnership: PropertyOwnership,
        update: PropertyOwnershipUpdateModel,
        expectedEmailBullets: List<String>,
    ) {
        // Arrange
        update.licenceNumber?.let {
            val updateLicence = License(update.licensingType!!, it)
            whenever(mockLicenseService.updateLicence(propertyOwnership.license, update.licensingType, update.licenceNumber))
                .thenReturn(updateLicence)
        }

        whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id))
            .thenReturn(propertyOwnership)

        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://example.com"))

        // Act
        propertyOwnershipService.updatePropertyOwnership(propertyOwnership.id, update) {}

        // Assert
        val expectedRegistrationNumber = RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber)
        verify(emailNotificationService).sendEmail(
            eq(propertyOwnership.primaryLandlord.email),
            argThat { email ->
                email.updatedBullets.bulletPoints.containsAll(expectedEmailBullets) &&
                    email.updatedBullets.bulletPoints.size == expectedEmailBullets.size &&
                    email.singleLineAddress == propertyOwnership.property.address.singleLineAddress &&
                    email.registrationNumber == expectedRegistrationNumber.toString()
            },
        )
    }

    @Test
    fun `when checkUpdateIsValid throws an exception, no update occurs`() {
        // Arrange
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = 1,
                ownershipType = OwnershipType.FREEHOLD,
                currentNumHouseholds = 2,
                currentNumTenants = 6,
                license = License(LicensingType.SELECTIVE_LICENCE, "licenceNumberSelective"),
            )
        val originalOwnershipType = propertyOwnership.ownershipType
        val originalNumberOfHouseholds = propertyOwnership.currentNumHouseholds
        val originalNumberOfPeople = propertyOwnership.currentNumTenants
        val originalLicenceType = propertyOwnership.license?.licenseType
        val originalLicenceNumber = propertyOwnership.license?.licenseNumber

        val updateLicence = License(LicensingType.HMO_MANDATORY_LICENCE, "licenceNumberMandatory")
        val updateModel =
            PropertyOwnershipUpdateModel(
                ownershipType = OwnershipType.LEASEHOLD,
                numberOfHouseholds = 1,
                numberOfPeople = 2,
                licensingType = updateLicence.licenseType,
                licenceNumber = updateLicence.licenseNumber,
            )

        // Act
        try {
            propertyOwnershipService.updatePropertyOwnership(propertyOwnership.id, updateModel) { throw Exception("Invalid update") }
        } catch (_: Exception) {
            // Expected exception, do nothing
        }

        // Assert
        assertEquals(originalOwnershipType, propertyOwnership.ownershipType)
        assertEquals(originalNumberOfHouseholds, propertyOwnership.currentNumHouseholds)
        assertEquals(originalNumberOfPeople, propertyOwnership.currentNumTenants)
        assertEquals(originalLicenceType, propertyOwnership.license?.licenseType)
        assertEquals(originalLicenceNumber, propertyOwnership.license?.licenseNumber)
    }

    @Test
    fun `updatePropertyOwnership removes the licence when the licence service returns no licence`() {
        // Arrange
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                license = License(LicensingType.SELECTIVE_LICENCE, "licenceNumberSelective"),
            )

        val updateModel =
            PropertyOwnershipUpdateModel(
                ownershipType = OwnershipType.LEASEHOLD,
                numberOfHouseholds = 1,
                numberOfPeople = 2,
                licensingType = LicensingType.NO_LICENSING,
                licenceNumber = null,
            )

        whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
            propertyOwnership,
        )
        whenever(
            mockLicenseService.updateLicence(propertyOwnership.license, updateModel.licensingType, updateModel.licenceNumber),
        ).thenReturn(null)

        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://example.com"))

        // Act
        propertyOwnershipService.updatePropertyOwnership(propertyOwnership.id, updateModel) {}

        // Assert
        assertNull(propertyOwnership.license)
    }

    @Test
    fun `deletePropertyOwnership calls delete on the propertyOwnershipRepository`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership()

        propertyOwnershipService.deletePropertyOwnership(propertyOwnership)

        verify(mockPropertyOwnershipRepository).delete(propertyOwnership)
    }

    @Test
    fun `deletePropertyOwnerships deletes a list from the propertyOwnershipRepository`() {
        val propertyOwnerships = listOf(MockLandlordData.createPropertyOwnership(), MockLandlordData.createPropertyOwnership())

        propertyOwnershipService.deletePropertyOwnerships(propertyOwnerships)

        verify(mockPropertyOwnershipRepository).deleteAll(propertyOwnerships)
    }

    @Test
    fun `retrieveAllPropertiesForLandlord gets a list of property ownerships`() {
        // Arrange
        val expectedPropertyOwnerships =
            listOf(
                MockLandlordData.createPropertyOwnership(),
                MockLandlordData.createPropertyOwnership(),
            )
        val baseUserId = "user-id"

        whenever(mockPropertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_Id(baseUserId)).thenReturn(expectedPropertyOwnerships)

        // Act
        val propertyOwnerships = propertyOwnershipService.retrieveAllPropertiesForLandlord(baseUserId)

        // Assert
        assertEquals(expectedPropertyOwnerships, propertyOwnerships)
    }

    @Test
    fun `deleteIncompleteComplianceForm deletes the corresponding form context and sets its reference to null`() {
        val incompleteComplianceForm = MockLandlordData.createPropertyRegistrationFormContext()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(incompleteComplianceForm = incompleteComplianceForm)
        whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(propertyOwnership)

        propertyOwnershipService.deleteIncompleteComplianceForm(propertyOwnership.id)

        verify(mockFormContextService).deleteFormContext(incompleteComplianceForm)
        assertNull(propertyOwnership.incompleteComplianceForm)
    }

    @Nested
    inner class GetIncompleteCompliancesForLandlord {
        private val principalName = "principalName"

        @Test
        fun `getIncompleteCompliancesForLandlord returns a list of incomplete compliances`() {
            // Arrange
            val incompleteComplianceForm = MockLandlordData.createPropertyComplianceFormContext()
            val propertyOwnershipWithIncompleteCompliance =
                MockLandlordData.createPropertyOwnership(currentNumTenants = 1, incompleteComplianceForm = incompleteComplianceForm)
            val properties =
                listOf(
                    MockLandlordData.createPropertyOwnership(currentNumTenants = 0, incompleteComplianceForm = null),
                    MockLandlordData.createPropertyOwnership(currentNumTenants = 0, incompleteComplianceForm = incompleteComplianceForm),
                    MockLandlordData.createPropertyOwnership(currentNumTenants = 1, incompleteComplianceForm = null),
                    propertyOwnershipWithIncompleteCompliance,
                )

            whenever(
                mockPropertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndProperty_Status(
                    principalName,
                    RegistrationStatus.REGISTERED,
                ),
            ).thenReturn(properties)

            // Act
            val returnedIncompleteCompliances = propertyOwnershipService.getIncompleteCompliancesForLandlord(principalName)

            // Assert
            val expectedIncompleteCompliances =
                listOf(ComplianceStatusDataModel.fromIncompleteComplianceForm(propertyOwnershipWithIncompleteCompliance))
            assertEquals(expectedIncompleteCompliances, returnedIncompleteCompliances)
        }

        @Test
        fun `getIncompleteCompliancesForLandlord returns an emptyList if the landlord has no properties`() {
            // Arrange
            whenever(
                mockPropertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndProperty_Status(
                    principalName,
                    RegistrationStatus.REGISTERED,
                ),
            ).thenReturn(emptyList())

            // Act
            val incompleteCompliances = propertyOwnershipService.getIncompleteCompliancesForLandlord(principalName)

            assertTrue(incompleteCompliances.isEmpty())
        }
    }

    @Nested
    inner class GetNumberOfIncompleteCompliancesForLandlord {
        val principalName = "principalName"

        @Test
        fun `returns the number of incomplete compliances for a landlord`() {
            // Arrange
            val expectedNumberOfIncompleteCompliances = 1
            val properties =
                listOf(
                    MockLandlordData.createPropertyOwnership(currentNumTenants = 3, incompleteComplianceForm = null),
                    MockLandlordData.createPropertyOwnership(
                        currentNumTenants = 2,
                        incompleteComplianceForm = MockLandlordData.createPropertyComplianceFormContext(),
                    ),
                )
            whenever(
                @Suppress("ktlint:standard:max-line-length")
                mockPropertyOwnershipRepository
                    .countByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndProperty_StatusAndCurrentNumTenantsIsGreaterThanAndIncompleteComplianceFormNotNull(
                        principalName,
                        RegistrationStatus.REGISTERED,
                        0,
                    ),
            ).thenReturn(1L)

            // Act
            val numberOfIncompleteCompliances = propertyOwnershipService.getNumberOfIncompleteCompliancesForLandlord(principalName)

            // Assert
            assertEquals(expectedNumberOfIncompleteCompliances, numberOfIncompleteCompliances)
        }

        @Test
        fun `returns 0 if there are no incomplete compliances for a landlord`() {
            // Arrange
            val expectedNumberOfIncompleteCompliances = 0
            whenever(
                @Suppress("ktlint:standard:max-line-length")
                mockPropertyOwnershipRepository
                    .countByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndProperty_StatusAndCurrentNumTenantsIsGreaterThanAndIncompleteComplianceFormNotNull(
                        principalName,
                        RegistrationStatus.REGISTERED,
                        0,
                    ),
            ).thenReturn(0L)

            // Act
            val numberOfIncompleteCompliances = propertyOwnershipService.getNumberOfIncompleteCompliancesForLandlord(principalName)

            // Assert
            assertEquals(expectedNumberOfIncompleteCompliances, numberOfIncompleteCompliances)
        }
    }

    companion object {
        fun occupiedPropertyOwnership() =
            MockLandlordData.createPropertyOwnership(
                ownershipType = OwnershipType.FREEHOLD,
                currentNumHouseholds = 2,
                currentNumTenants = 4,
                license = License(LicensingType.SELECTIVE_LICENCE, "licenceNumber"),
            )

        fun unoccupiedPropertyOwnership() =
            MockLandlordData.createPropertyOwnership(
                ownershipType = OwnershipType.FREEHOLD,
                currentNumHouseholds = 0,
                currentNumTenants = 0,
                license = License(LicensingType.SELECTIVE_LICENCE, "licenceNumber"),
            )

        @JvmStatic
        fun updatesAndConfirmationEmails(): List<Arguments?> =
            listOf(
                Arguments.of(
                    occupiedPropertyOwnership(),
                    PropertyOwnershipUpdateModel(
                        ownershipType = OwnershipType.LEASEHOLD,
                        numberOfHouseholds = 1,
                        numberOfPeople = 2,
                        licensingType = LicensingType.HMO_MANDATORY_LICENCE,
                        licenceNumber = "licenceNumberMandatory",
                    ),
                    listOf(
                        "ownership type",
                        "licensing information",
                        "the number of households living in this property",
                        "the number of people living in this property",
                    ),
                ),
                Arguments.of(
                    occupiedPropertyOwnership(),
                    PropertyOwnershipUpdateModel(
                        ownershipType = OwnershipType.FREEHOLD,
                        numberOfHouseholds = 0,
                        numberOfPeople = 0,
                        licensingType = null,
                        licenceNumber = null,
                    ),
                    listOf("whether the property is occupied by tenants"),
                ),
                Arguments.of(
                    unoccupiedPropertyOwnership(),
                    PropertyOwnershipUpdateModel(
                        ownershipType = null,
                        numberOfHouseholds = 3,
                        numberOfPeople = 5,
                        licensingType = LicensingType.NO_LICENSING,
                        licenceNumber = null,
                    ),
                    listOf("licensing information", "whether the property is occupied by tenants"),
                ),
            )
    }
}
