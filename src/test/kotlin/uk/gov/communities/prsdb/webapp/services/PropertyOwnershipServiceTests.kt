package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Named
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
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor.Companion.overrideBackLinkForUrl
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.exceptions.RepositoryQueryTimeoutException
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyOwnershipUpdateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyUpdateConfirmation
import uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels.PropertySearchResultViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.RegisteredPropertyLandlordViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.RegisteredPropertyLocalCouncilViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockOneLoginUserData
import java.net.URI

@ExtendWith(MockitoExtension::class)
class PropertyOwnershipServiceTests {
    @Mock
    private lateinit var mockPropertyOwnershipRepository: PropertyOwnershipRepository

    @Mock
    private lateinit var mockRegistrationNumberService: RegistrationNumberService

    @Mock
    private lateinit var mockLocalCouncilDataService: LocalCouncilDataService

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
        val ownershipType = OwnershipType.FREEHOLD
        val households = 1
        val tenants = 2
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)
        val landlord = MockLandlordData.createLandlord()
        val propertyBuildType = PropertyType.FLAT
        val address = MockLandlordData.createAddress("11 Example Road, EG1 2AB")
        val license = License()
        val incompleteComplianceForm = FormContext(JourneyType.PROPERTY_COMPLIANCE, landlord.baseUser)

        val expectedPropertyOwnership =
            PropertyOwnership(
                ownershipType = ownershipType,
                currentNumHouseholds = households,
                currentNumTenants = tenants,
                registrationNumber = registrationNumber,
                primaryLandlord = landlord,
                propertyBuildType = propertyBuildType,
                address = address,
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
            propertyBuildType,
            address,
            license,
        )

        val propertyOwnershipCaptor = captor<PropertyOwnership>()
        verify(mockPropertyOwnershipRepository).save(propertyOwnershipCaptor.capture())
        assertTrue(ReflectionEquals(expectedPropertyOwnership).matches(propertyOwnershipCaptor.value))
    }

    @Test
    fun `createPropertyOwnership can create a property ownership with no license`() {
        val ownershipType = OwnershipType.FREEHOLD
        val households = 1
        val tenants = 2
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)
        val landlord = MockLandlordData.createLandlord()
        val propertyBuildType = PropertyType.FLAT
        val address = MockLandlordData.createAddress("11 Example Road, EG1 2AB")
        val incompleteComplianceForm = FormContext(JourneyType.PROPERTY_COMPLIANCE, landlord.baseUser)

        val expectedPropertyOwnership =
            PropertyOwnership(
                ownershipType = ownershipType,
                currentNumHouseholds = households,
                currentNumTenants = tenants,
                registrationNumber = registrationNumber,
                primaryLandlord = landlord,
                propertyBuildType = propertyBuildType,
                address = address,
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
            propertyBuildType,
            address,
        )

        val propertyOwnershipCaptor = captor<PropertyOwnership>()
        verify(mockPropertyOwnershipRepository).save(propertyOwnershipCaptor.capture())
        assertTrue(ReflectionEquals(expectedPropertyOwnership).matches(propertyOwnershipCaptor.value))
    }

    @Nested
    inner class GetLandlordRegisteredPropertiesDetails {
        private val landlord = MockLandlordData.createLandlord()
        private val localCouncil = LocalCouncil(11, "DERBYSHIRE DALES DISTRICT COUNCIL", "1045")
        private val expectedPropertyLicence = "forms.checkPropertyAnswers.propertyDetails.noLicensing"
        private val expectedIsTenantedMessageKey = "commonText.no"
        private val expectedCurrentUrlKey = 101

        private val propertyOwnership1 =
            MockLandlordData.createPropertyOwnership(
                primaryLandlord = landlord,
                address = MockLandlordData.createAddress("11 Example Road, EG1 2AB", localCouncil),
                registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456),
                license = null,
                currentNumTenants = 0,
            )

        private val propertyOwnership2 =
            MockLandlordData.createPropertyOwnership(
                primaryLandlord = landlord,
                address = MockLandlordData.createAddress("12 Example Road, EG1 2AB", localCouncil),
                registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 654321),
                license = null,
                currentNumTenants = 0,
            )

        private val landlordsProperties: List<PropertyOwnership> = listOf(propertyOwnership1, propertyOwnership2)

        @Test
        fun `Returns a list of Landlords properties in correctly formatted data model from landlords BaseUser_Id`() {
            whenever(
                mockPropertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrue(landlord.baseUser.id),
            ).thenReturn(landlordsProperties)

            whenever(mockBackUrlStorageService.storeCurrentUrlReturningKey()).thenReturn(expectedCurrentUrlKey)

            val expectedResults: List<RegisteredPropertyLandlordViewModel> =
                listOf(
                    RegisteredPropertyLandlordViewModel(
                        address = propertyOwnership1.address.singleLineAddress,
                        registrationNumber =
                            RegistrationNumberDataModel
                                .fromRegistrationNumber(propertyOwnership1.registrationNumber)
                                .toString(),
                        recordLink =
                            PropertyDetailsController
                                .getPropertyDetailsPath(propertyOwnership1.id)
                                .overrideBackLinkForUrl(expectedCurrentUrlKey),
                    ),
                    RegisteredPropertyLandlordViewModel(
                        address = propertyOwnership2.address.singleLineAddress,
                        registrationNumber =
                            RegistrationNumberDataModel
                                .fromRegistrationNumber(propertyOwnership2.registrationNumber)
                                .toString(),
                        recordLink =
                            PropertyDetailsController
                                .getPropertyDetailsPath(propertyOwnership2.id)
                                .overrideBackLinkForUrl(expectedCurrentUrlKey),
                    ),
                )

            val result = propertyOwnershipService.getRegisteredPropertiesForLandlordUser(landlord.baseUser.id)

            assertTrue(result.size == 2)
            assertEquals(expectedResults, result)
        }

        @Test
        fun `Returns a list of Landlords properties in correctly formatted data model from landlords Id`() {
            whenever(
                mockPropertyOwnershipRepository.findAllByPrimaryLandlord_IdAndIsActiveTrue(landlord.id),
            ).thenReturn(landlordsProperties)

            whenever(mockBackUrlStorageService.storeCurrentUrlReturningKey()).thenReturn(expectedCurrentUrlKey)

            val expectedResults: List<RegisteredPropertyLocalCouncilViewModel> =
                listOf(
                    RegisteredPropertyLocalCouncilViewModel(
                        address = propertyOwnership1.address.singleLineAddress,
                        registrationNumber =
                            RegistrationNumberDataModel
                                .fromRegistrationNumber(propertyOwnership1.registrationNumber)
                                .toString(),
                        localCouncilName = localCouncil.name,
                        licenseTypeMessageKey = expectedPropertyLicence,
                        isTenantedMessageKey = expectedIsTenantedMessageKey,
                        recordLink =
                            PropertyDetailsController
                                .getPropertyDetailsPath(propertyOwnership1.id, isLocalCouncilView = true)
                                .overrideBackLinkForUrl(expectedCurrentUrlKey),
                    ),
                    RegisteredPropertyLocalCouncilViewModel(
                        address = propertyOwnership2.address.singleLineAddress,
                        registrationNumber =
                            RegistrationNumberDataModel
                                .fromRegistrationNumber(propertyOwnership2.registrationNumber)
                                .toString(),
                        localCouncilName = localCouncil.name,
                        licenseTypeMessageKey = expectedPropertyLicence,
                        isTenantedMessageKey = expectedIsTenantedMessageKey,
                        recordLink =
                            PropertyDetailsController
                                .getPropertyDetailsPath(propertyOwnership2.id, isLocalCouncilView = true)
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
        fun `throws not found error if user is not primary landlord or an lc user`() {
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
        fun `returns property ownership when user is an lc user`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership()
            val localCouncilUser =
                MockLocalCouncilData.createLocalCouncilUser(
                    MockOneLoginUserData.createOneLoginUser("not-the-landlord"),
                    MockLocalCouncilData.createLocalCouncil(),
                )
            val principalName = localCouncilUser.baseUser.id

            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
                propertyOwnership,
            )

            whenever(mockLocalCouncilDataService.getIsLocalCouncilUser(principalName)).thenReturn(true)

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

            whenever(mockLocalCouncilDataService.getIsLocalCouncilUser(principalName)).thenReturn(false)

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
        val lcBaseUserId = "id"
        val pageRequest = PageRequest.of(1, 10)
        val prnMatchingPropertyOwnership = listOf(MockLandlordData.createPropertyOwnership())
        val currentUrlKey = 13
        val expectedSearchResults =
            prnMatchingPropertyOwnership.map { PropertySearchResultViewModel.fromPropertyOwnership(it, currentUrlKey) }

        whenever(
            mockPropertyOwnershipRepository.searchMatchingPRN(searchPRN.number, lcBaseUserId, pageable = pageRequest),
        ).thenReturn(PageImpl(prnMatchingPropertyOwnership))

        whenever(mockBackUrlStorageService.storeCurrentUrlReturningKey()).thenReturn(currentUrlKey)

        val searchResults =
            propertyOwnershipService.searchForProperties(
                searchPRN.toString(),
                lcBaseUserId,
                requestedPageIndex = pageRequest.pageNumber,
                pageSize = pageRequest.pageSize,
            )

        assertEquals(expectedSearchResults, searchResults.content)
    }

    @Test
    fun `searchForProperties returns no results when the search term is a non-property registration number`() {
        val nonPropertyRegNum = RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, 123)
        val lcBaseUserId = "id"
        val pageRequest = PageRequest.of(1, 10)

        whenever(
            mockPropertyOwnershipRepository.searchMatching(nonPropertyRegNum.toString(), lcBaseUserId, pageable = pageRequest),
        ).thenReturn(Page.empty())

        val searchResults =
            propertyOwnershipService.searchForProperties(
                nonPropertyRegNum.toString(),
                lcBaseUserId,
                requestedPageIndex = pageRequest.pageNumber,
                pageSize = pageRequest.pageSize,
            )

        verify(mockPropertyOwnershipRepository, never()).searchMatchingPRN(
            nonPropertyRegNum.number,
            lcBaseUserId,
            pageable = pageRequest,
        )
        assertEquals(emptyList<PropertySearchResultViewModel>(), searchResults.content)
    }

    @Test
    fun `searchForProperties returns a single matching property when the search term is a UPRN`() {
        val searchUPRN = "123"
        val lcBaseUserId = "id"
        val pageRequest = PageRequest.of(1, 10)
        val currentUrlKey = 23

        val uprnMatchingPropertyOwnership =
            listOf(MockLandlordData.createPropertyOwnership(address = MockLandlordData.createAddress(uprn = searchUPRN.toLong())))
        val expectedSearchResults =
            uprnMatchingPropertyOwnership.map { PropertySearchResultViewModel.fromPropertyOwnership(it, currentUrlKey) }

        whenever(
            mockPropertyOwnershipRepository.searchMatchingUPRN(searchUPRN.toLong(), lcBaseUserId, pageable = pageRequest),
        ).thenReturn(PageImpl(uprnMatchingPropertyOwnership))
        whenever(mockBackUrlStorageService.storeCurrentUrlReturningKey()).thenReturn(currentUrlKey)

        val searchResults =
            propertyOwnershipService.searchForProperties(
                searchUPRN,
                lcBaseUserId,
                requestedPageIndex = pageRequest.pageNumber,
                pageSize = pageRequest.pageSize,
            )

        assertEquals(expectedSearchResults, searchResults.content)
    }

    @Test
    fun `searchForProperties returns a collection of fuzzy matches when the search term is not a PRN or UPRN`() {
        val searchTerm = "road"
        val lcBaseUserId = "id"
        val urlKey = 7
        val pageRequest = PageRequest.of(1, 10)

        val fuzzyMatchingPropertyOwnerships =
            listOf(MockLandlordData.createPropertyOwnership(), MockLandlordData.createPropertyOwnership())
        val expectedSearchResults =
            fuzzyMatchingPropertyOwnerships.map { PropertySearchResultViewModel.fromPropertyOwnership(it, 7) }

        whenever(
            mockPropertyOwnershipRepository.searchMatching(searchTerm, lcBaseUserId, pageable = pageRequest),
        ).thenReturn(PageImpl(fuzzyMatchingPropertyOwnerships))
        whenever(mockBackUrlStorageService.storeCurrentUrlReturningKey()).thenReturn(urlKey)

        val searchResults =
            propertyOwnershipService.searchForProperties(
                searchTerm,
                lcBaseUserId,
                requestedPageIndex = pageRequest.pageNumber,
                pageSize = pageRequest.pageSize,
            )

        assertEquals(expectedSearchResults, searchResults.content)
    }

    @Test
    fun `searchForProperties returns the requested page of properties`() {
        val searchTerm = "searchTerm"
        val lcBaseUserId = "id"
        val pageSize = 25
        val matchingProperties = (1..40).map { MockLandlordData.createPropertyOwnership() }

        val pageIndex1 = 0
        val urlKey1 = 37
        val pageRequest1 = PageRequest.of(pageIndex1, pageSize)
        val matchingPropertiesPage1 = matchingProperties.subList(0, pageSize)
        val expectedPage1SearchResults = matchingPropertiesPage1.map { PropertySearchResultViewModel.fromPropertyOwnership(it, urlKey1) }

        val pageIndex2 = 1
        val urlKey2 = 41
        val pageRequest2 = PageRequest.of(pageIndex2, pageSize)
        val matchingPropertiesPage2 = matchingProperties.subList(pageSize, matchingProperties.size)
        val expectedPage2SearchResults = matchingPropertiesPage2.map { PropertySearchResultViewModel.fromPropertyOwnership(it, urlKey2) }

        whenever(mockPropertyOwnershipRepository.searchMatching(searchTerm, lcBaseUserId, pageable = pageRequest1))
            .thenReturn(PageImpl(matchingPropertiesPage1))
        whenever(mockPropertyOwnershipRepository.searchMatching(searchTerm, lcBaseUserId, pageable = pageRequest2))
            .thenReturn(PageImpl(matchingPropertiesPage2))

        whenever(mockBackUrlStorageService.storeCurrentUrlReturningKey()).thenReturn(urlKey1)
        val searchResults1 =
            propertyOwnershipService.searchForProperties(searchTerm, lcBaseUserId, requestedPageIndex = pageIndex1)

        whenever(mockBackUrlStorageService.storeCurrentUrlReturningKey()).thenReturn(urlKey2)
        val searchResults2 =
            propertyOwnershipService.searchForProperties(searchTerm, lcBaseUserId, requestedPageIndex = pageIndex2)

        assertEquals(expectedPage1SearchResults, searchResults1.content)
        assertEquals(expectedPage2SearchResults, searchResults2.content)
    }

    @Test
    fun `searchForProperties throws an exception when fuzzy searching times out`() {
        // Arrange
        val searchTerm = "searchTerm"
        val lcBaseUserId = "id"
        val pageRequest = PageRequest.of(1, 10)

        whenever(
            mockPropertyOwnershipRepository.searchMatching(searchTerm, lcBaseUserId, pageable = pageRequest),
        ).thenThrow(QueryTimeoutException("Query timed out"))

        // Act & Assert
        assertThrows<RepositoryQueryTimeoutException> {
            propertyOwnershipService.searchForProperties(
                searchTerm,
                lcBaseUserId,
                requestedPageIndex = pageRequest.pageNumber,
                pageSize = pageRequest.pageSize,
            )
        }
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
    fun `updatePropertyOwnership does not send a confirmation email if the update is empty`() {
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
                null,
                null,
                null,
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
    @ParameterizedTest(name = "[{index}] For {0} where the update {1} reports {2}")
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
                    email.singleLineAddress == propertyOwnership.address.singleLineAddress &&
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
        val propertyOwnershipId = 1L

        propertyOwnershipService.deletePropertyOwnership(propertyOwnershipId)

        verify(mockPropertyOwnershipRepository).deleteById(propertyOwnershipId)
    }

    @Test
    fun `deletePropertyOwnerships deletes a list from the propertyOwnershipRepository`() {
        val propertyOwnerships = listOf(MockLandlordData.createPropertyOwnership(), MockLandlordData.createPropertyOwnership())

        propertyOwnershipService.deletePropertyOwnerships(propertyOwnerships)

        verify(mockPropertyOwnershipRepository).deleteAll(propertyOwnerships)
    }

    @Test
    fun `retrieveAllActivePropertiesForLandlord gets a list of property ownerships`() {
        // Arrange
        val expectedPropertyOwnerships =
            listOf(
                MockLandlordData.createPropertyOwnership(),
                MockLandlordData.createPropertyOwnership(),
            )
        val baseUserId = "user-id"

        whenever(
            mockPropertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrue(baseUserId),
        ).thenReturn(expectedPropertyOwnerships)

        // Act
        val propertyOwnerships = propertyOwnershipService.retrieveAllActivePropertiesForLandlord(baseUserId)

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
                mockPropertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrue(principalName),
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
                mockPropertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrue(principalName),
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

            whenever(
                mockPropertyOwnershipRepository
                    .countByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndCurrentNumTenantsIsGreaterThanAndIncompleteComplianceFormNotNull(
                        principalName,
                        0,
                    ),
            ).thenReturn(1)

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
                mockPropertyOwnershipRepository
                    .countByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndCurrentNumTenantsIsGreaterThanAndIncompleteComplianceFormNotNull(
                        principalName,
                        0,
                    ),
            ).thenReturn(0)

            // Act
            val numberOfIncompleteCompliances = propertyOwnershipService.getNumberOfIncompleteCompliancesForLandlord(principalName)

            // Assert
            assertEquals(expectedNumberOfIncompleteCompliances, numberOfIncompleteCompliances)
        }
    }

    companion object {
        fun occupiedPropertyOwnership(): Named<PropertyOwnership> =
            Named.of(
                "an occupied property",
                MockLandlordData.createPropertyOwnership(
                    ownershipType = OwnershipType.FREEHOLD,
                    currentNumHouseholds = 2,
                    currentNumTenants = 4,
                    license = License(LicensingType.SELECTIVE_LICENCE, "licenceNumber"),
                ),
            )

        fun unoccupiedPropertyOwnership(): Named<PropertyOwnership> =
            Named.of(
                "an unoccupied property",
                MockLandlordData.createPropertyOwnership(
                    ownershipType = OwnershipType.FREEHOLD,
                    currentNumHouseholds = 0,
                    currentNumTenants = 0,
                    license = License(LicensingType.SELECTIVE_LICENCE, "licenceNumber"),
                ),
            )

        @JvmStatic
        fun updatesAndConfirmationEmails(): List<Arguments> {
            val referenceOccupied = occupiedPropertyOwnership().payload
            val referenceUnoccupied = unoccupiedPropertyOwnership().payload
            return listOf(
                Arguments.of(
                    occupiedPropertyOwnership(),
                    Named.of(
                        "changes all fields such that the property is still occupied",
                        PropertyOwnershipUpdateModel(
                            ownershipType = OwnershipType.LEASEHOLD,
                            numberOfHouseholds = 1,
                            numberOfPeople = 2,
                            licensingType = LicensingType.HMO_MANDATORY_LICENCE,
                            licenceNumber = "licenceNumberMandatory",
                        ),
                    ),
                    Named.of(
                        "all fields changed except occupancy",
                        listOf(
                            "ownership type",
                            "licensing information",
                            "the number of households living in this property",
                            "the number of people living in this property",
                        ),
                    ),
                ),
                Arguments.of(
                    occupiedPropertyOwnership(),
                    Named.of(
                        "changes it to unoccupied",
                        PropertyOwnershipUpdateModel(
                            ownershipType = null,
                            numberOfHouseholds = 0,
                            numberOfPeople = 0,
                            licensingType = null,
                            licenceNumber = null,
                        ),
                    ),
                    Named.of(
                        "an occupancy change",
                        listOf("whether the property is occupied by tenants"),
                    ),
                ),
                Arguments.of(
                    unoccupiedPropertyOwnership(),
                    Named.of(
                        "changes it to occupied",
                        PropertyOwnershipUpdateModel(
                            ownershipType = null,
                            numberOfHouseholds = 3,
                            numberOfPeople = 5,
                            licensingType = null,
                            licenceNumber = null,
                        ),
                    ),
                    Named.of(
                        "an occupancy change",
                        listOf("whether the property is occupied by tenants"),
                    ),
                ),
                Arguments.of(
                    unoccupiedPropertyOwnership(),
                    Named.of(
                        "changes all non-occupancy fields to the same values as before",
                        PropertyOwnershipUpdateModel(
                            ownershipType = referenceUnoccupied.ownershipType,
                            numberOfHouseholds = null,
                            numberOfPeople = null,
                            licensingType = referenceUnoccupied.license?.licenseType,
                            licenceNumber = referenceUnoccupied.license?.licenseNumber,
                        ),
                    ),
                    Named.of(
                        "all non-occupancy fields changed",
                        listOf(
                            "ownership type",
                            "licensing information",
                        ),
                    ),
                ),
                Arguments.of(
                    unoccupiedPropertyOwnership(),
                    Named.of(
                        "changes it from empty to empty",
                        PropertyOwnershipUpdateModel(
                            ownershipType = null,
                            numberOfHouseholds = 0,
                            numberOfPeople = 0,
                            licensingType = null,
                            licenceNumber = null,
                        ),
                    ),
                    Named.of(
                        "an occupancy change",
                        listOf(
                            "whether the property is occupied by tenants",
                        ),
                    ),
                ),
                Arguments.of(
                    occupiedPropertyOwnership(),
                    Named.of(
                        "changes the number of households and people to what they were before",
                        PropertyOwnershipUpdateModel(
                            ownershipType = null,
                            numberOfHouseholds = referenceOccupied.currentNumHouseholds,
                            numberOfPeople = referenceOccupied.currentNumTenants,
                            licensingType = null,
                            licenceNumber = null,
                        ),
                    ),
                    Named.of(
                        "a change to the number of households and people",
                        listOf(
                            "the number of households living in this property",
                            "the number of people living in this property",
                        ),
                    ),
                ),
            )
        }
    }
}
