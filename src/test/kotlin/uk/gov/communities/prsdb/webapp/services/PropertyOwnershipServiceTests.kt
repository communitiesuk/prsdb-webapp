package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor.Companion.overrideBackLinkForUrl
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.exceptions.RepositoryQueryTimeoutException
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels.PropertySearchResultViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.RegisteredPropertyLandlordViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.RegisteredPropertyLocalCouncilViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPrsdbUserData
import java.math.BigDecimal
import java.time.temporal.ChronoUnit

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
    private lateinit var mockBackUrlStorageService: BackUrlStorageService

    @InjectMocks
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @Test
    fun `createPropertyOwnership creates a property ownership`() {
        val ownershipType = OwnershipType.FREEHOLD
        val households = 1
        val tenants = 2
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)
        val landlord = MockLandlordData.createLandlord()
        val propertyBuildType = PropertyType.OTHER
        val customPropertyType = "End terrace"
        val address = MockLandlordData.createAddress("11 Example Road, EG1 2AB")
        val license = License()
        val numberOfBedrooms = 1
        val billsIncludedList = "Electricity, Water"
        val customBillsIncluded = "Internet"
        val furnishedStatus = FurnishedStatus.FURNISHED
        val rentFrequency = RentFrequency.OTHER
        val customRentFrequency = "Fortnightly"
        val rentAmount = 123.toBigDecimal()

        val expectedPropertyOwnership =
            PropertyOwnership(
                ownershipType = ownershipType,
                currentNumHouseholds = households,
                currentNumTenants = tenants,
                registrationNumber = registrationNumber,
                primaryLandlord = landlord,
                propertyBuildType = propertyBuildType,
                customPropertyType = customPropertyType,
                address = address,
                license = license,
                numBedrooms = numberOfBedrooms,
                billsIncludedList = billsIncludedList,
                customBillsIncluded = customBillsIncluded,
                furnishedStatus = furnishedStatus,
                rentFrequency = rentFrequency,
                customRentFrequency = customRentFrequency,
                rentAmount = rentAmount,
            )

        whenever(mockRegistrationNumberService.createRegistrationNumber(RegistrationNumberType.PROPERTY)).thenReturn(
            registrationNumber,
        )
        whenever(mockPropertyOwnershipRepository.save(any<PropertyOwnership>())).thenReturn(
            expectedPropertyOwnership,
        )

        propertyOwnershipService.createPropertyOwnership(
            ownershipType = ownershipType,
            numberOfHouseholds = households,
            numberOfPeople = tenants,
            primaryLandlord = landlord,
            propertyBuildType = propertyBuildType,
            customPropertyType = customPropertyType,
            address = address,
            license = license,
            numBedrooms = numberOfBedrooms,
            billsIncludedList = billsIncludedList,
            customBillsIncluded = customBillsIncluded,
            furnishedStatus = furnishedStatus,
            rentFrequency = rentFrequency,
            customRentFrequency = customRentFrequency,
            rentAmount = rentAmount,
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
        val propertyBuildType = PropertyType.OTHER
        val customPropertyType = "End terrace"
        val address = MockLandlordData.createAddress("11 Example Road, EG1 2AB")
        val numberOfBedrooms = 1
        val billsIncludedList = "Electricity, Water"
        val customBillsIncluded = "Internet"
        val furnishedStatus = FurnishedStatus.FURNISHED
        val rentFrequency = RentFrequency.OTHER
        val customRentFrequency = "Fortnightly"
        val rentAmount = 123.toBigDecimal()

        val expectedPropertyOwnership =
            PropertyOwnership(
                ownershipType = ownershipType,
                currentNumHouseholds = households,
                currentNumTenants = tenants,
                registrationNumber = registrationNumber,
                primaryLandlord = landlord,
                propertyBuildType = propertyBuildType,
                customPropertyType = customPropertyType,
                address = address,
                license = null,
                numBedrooms = numberOfBedrooms,
                billsIncludedList = billsIncludedList,
                customBillsIncluded = customBillsIncluded,
                furnishedStatus = furnishedStatus,
                rentFrequency = rentFrequency,
                customRentFrequency = customRentFrequency,
                rentAmount = rentAmount,
            )

        whenever(mockRegistrationNumberService.createRegistrationNumber(RegistrationNumberType.PROPERTY)).thenReturn(
            registrationNumber,
        )
        whenever(mockPropertyOwnershipRepository.save(any<PropertyOwnership>())).thenReturn(
            expectedPropertyOwnership,
        )

        propertyOwnershipService.createPropertyOwnership(
            ownershipType = ownershipType,
            numberOfHouseholds = households,
            numberOfPeople = tenants,
            primaryLandlord = landlord,
            propertyBuildType = propertyBuildType,
            customPropertyType = customPropertyType,
            address = address,
            numBedrooms = numberOfBedrooms,
            billsIncludedList = billsIncludedList,
            customBillsIncluded = customBillsIncluded,
            furnishedStatus = furnishedStatus,
            rentFrequency = rentFrequency,
            customRentFrequency = customRentFrequency,
            rentAmount = rentAmount,
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
                    MockPrsdbUserData.createPrsdbUser("not-the-landlord"),
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
                            baseUser = MockLandlordData.createPrsdbUser(baseUserId),
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
                            baseUser = MockLandlordData.createPrsdbUser("baseUserId"),
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
    fun `updateLicensing updates the property's license`() {
        // Arrange
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = 1,
                license = License(LicensingType.SELECTIVE_LICENCE, "licenceNumber"),
            )
        val newLicensingType = LicensingType.HMO_MANDATORY_LICENCE
        val newLicenceNumber = "newLicenceNumber"
        val updatedLicence = License(newLicensingType, newLicenceNumber)

        whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
            propertyOwnership,
        )
        whenever(
            mockLicenseService.updateLicence(propertyOwnership.license, newLicensingType, newLicenceNumber),
        ).thenReturn(updatedLicence)

        // Act
        propertyOwnershipService.updateLicensing(propertyOwnership.id, newLicensingType, newLicenceNumber)

        // Assert
        assertEquals(updatedLicence, propertyOwnership.license)
    }

    @Nested
    inner class UpdateOccupancy {
        @Test
        fun `updateOccupancy updates the property's occupancy status`() {
            // Arrange
            val propertyOwnership =
                MockLandlordData.createOccupiedPropertyOwnership(
                    id = 1,
                    currentNumTenants = 4,
                )
            val newNumberOfTenants = 5
            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
                propertyOwnership,
            )

            // Act
            propertyOwnershipService.updateOccupancy(
                propertyOwnership.id,
                numberOfPeople = newNumberOfTenants,
                numberOfHouseholds = propertyOwnership.currentNumHouseholds,
                numBedrooms = propertyOwnership.numBedrooms,
                billsIncludedList = propertyOwnership.billsIncludedList,
                customBillsIncluded = propertyOwnership.customBillsIncluded,
                furnishedStatus = propertyOwnership.furnishedStatus,
                rentFrequency = propertyOwnership.rentFrequency,
                customRentFrequency = propertyOwnership.customRentFrequency,
                rentAmount = propertyOwnership.rentAmount,
                initialLastModifiedDate = propertyOwnership.getMostRecentlyUpdated(),
            )

            // Assert
            assertEquals(newNumberOfTenants, propertyOwnership.currentNumTenants)
        }

        @Test
        fun `updateOccupancy throws exception when initialLastModifiedDate does not match current lastModifiedDate`() {
            // Arrange
            val propertyOwnership =
                MockLandlordData.createOccupiedPropertyOwnership()
            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
                propertyOwnership,
            )

            // Act & Assert
            val exception =
                assertThrows<UpdateConflictException> {
                    propertyOwnershipService.updateOccupancy(
                        propertyOwnership.id,
                        numberOfPeople = 6,
                        numberOfHouseholds = propertyOwnership.currentNumHouseholds,
                        numBedrooms = propertyOwnership.numBedrooms,
                        billsIncludedList = propertyOwnership.billsIncludedList,
                        customBillsIncluded = propertyOwnership.customBillsIncluded,
                        furnishedStatus = propertyOwnership.furnishedStatus,
                        rentFrequency = propertyOwnership.rentFrequency,
                        customRentFrequency = propertyOwnership.customRentFrequency,
                        rentAmount = propertyOwnership.rentAmount,
                        initialLastModifiedDate = propertyOwnership.getMostRecentlyUpdated().minus(1, ChronoUnit.MINUTES),
                    )
                }

            assertEquals(
                "The property ownership record has been updated since this update session started.",
                exception.message,
            )
        }
    }

    @Nested
    inner class UpdateHouseholdsAndTenants {
        @Test
        fun `updateHouseholdsAndTenants updates the property's households and tenants values`() {
            // Arrange
            val propertyOwnership =
                MockLandlordData.createOccupiedPropertyOwnership(
                    id = 1,
                    currentNumHouseholds = 2,
                    currentNumTenants = 4,
                )
            val newNumberOfHouseholds = 3
            val newNumberOfTenants = 5
            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
                propertyOwnership,
            )

            // Act
            propertyOwnershipService.updateHouseholdsAndTenants(
                propertyOwnership.id,
                numberOfPeople = newNumberOfTenants,
                numberOfHouseholds = newNumberOfHouseholds,
                initialLastModifiedDate = propertyOwnership.getMostRecentlyUpdated(),
            )

            // Assert
            assertEquals(newNumberOfTenants, propertyOwnership.currentNumTenants)
            assertEquals(newNumberOfHouseholds, propertyOwnership.currentNumHouseholds)
        }

        @Test
        fun `updateHouseholdsAndTenants throws exception when initialLastModifiedDate does not match current lastModifiedDate`() {
            // Arrange
            val propertyOwnership =
                MockLandlordData.createOccupiedPropertyOwnership()
            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
                propertyOwnership,
            )

            // Act & Assert
            val exception =
                assertThrows<UpdateConflictException> {
                    propertyOwnershipService.updateHouseholdsAndTenants(
                        propertyOwnership.id,
                        numberOfPeople = 6,
                        numberOfHouseholds = 6,
                        initialLastModifiedDate = propertyOwnership.getMostRecentlyUpdated().minus(1, ChronoUnit.MINUTES),
                    )
                }

            assertEquals(
                "The property ownership record has been updated since this update session started.",
                exception.message,
            )
        }
    }

    @Nested
    inner class UpdateBedrooms {
        @Test
        fun `updateBedrooms updates the property's number of bedrooms`() {
            // Arrange
            val propertyOwnership =
                MockLandlordData.createOccupiedPropertyOwnership(
                    id = 1,
                    numberOfBedrooms = 2,
                )
            val newNumberOfBedrooms = 3
            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
                propertyOwnership,
            )

            // Act
            propertyOwnershipService.updateBedrooms(
                propertyOwnership.id,
                numberOfBedrooms = newNumberOfBedrooms,
                initialLastModifiedDate = propertyOwnership.getMostRecentlyUpdated(),
            )

            // Assert
            assertEquals(newNumberOfBedrooms, propertyOwnership.numBedrooms)
        }

        @Test
        fun `updateBedrooms throws exception when initialLastModifiedDate does not match current lastModifiedDate`() {
            // Arrange
            val propertyOwnership =
                MockLandlordData.createOccupiedPropertyOwnership()
            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
                propertyOwnership,
            )

            // Act & Assert
            val exception =
                assertThrows<UpdateConflictException> {
                    propertyOwnershipService.updateBedrooms(
                        propertyOwnership.id,
                        numberOfBedrooms = 4,
                        initialLastModifiedDate = propertyOwnership.getMostRecentlyUpdated().minus(1, ChronoUnit.MINUTES),
                    )
                }

            assertEquals(
                "The property ownership record has been updated since this update session started.",
                exception.message,
            )
        }
    }

    @Nested
    inner class UpdateRentIncludesBills {
        @Test
        fun `updateRentIncludesBills updates the property's bills included values`() {
            // Arrange
            val propertyOwnership =
                MockLandlordData.createOccupiedPropertyOwnership(
                    id = 1,
                    billsIncludedList = "ELECTRICITY,WATER,SOMETHING_ELSE",
                    customBillsIncluded = "Cat sitting",
                )
            val newBillsIncludedList = "GAS,BROADBAND,SOMETHING_ELSE"
            val newCustomBillsIncluded = "Dog grooming"
            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
                propertyOwnership,
            )

            // Act
            propertyOwnershipService.updateRentIncludesBills(
                propertyOwnership.id,
                billsIncludedList = newCustomBillsIncluded,
                customBillsIncluded = newBillsIncludedList,
                initialLastModifiedDate = propertyOwnership.getMostRecentlyUpdated(),
            )

            // Assert
            assertEquals(newCustomBillsIncluded, propertyOwnership.billsIncludedList)
            assertEquals(newBillsIncludedList, propertyOwnership.customBillsIncluded)
        }

        @Test
        fun `updateRentIncludesBills can update the property's bills included values to null`() {
            // Arrange
            val propertyOwnership =
                MockLandlordData.createOccupiedPropertyOwnership(
                    id = 1,
                    billsIncludedList = "ELECTRICITY,WATER,SOMETHING_ELSE",
                    customBillsIncluded = "Cat sitting",
                )
            val newBillsIncludedList = "GAS,BROADBAND,SOMETHING_ELSE"
            val newCustomBillsIncluded = "Dog grooming"
            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
                propertyOwnership,
            )

            // Act
            propertyOwnershipService.updateRentIncludesBills(
                propertyOwnership.id,
                billsIncludedList = newCustomBillsIncluded,
                customBillsIncluded = newBillsIncludedList,
                initialLastModifiedDate = propertyOwnership.getMostRecentlyUpdated(),
            )

            // Assert
            assertEquals(newCustomBillsIncluded, propertyOwnership.billsIncludedList)
            assertEquals(newBillsIncludedList, propertyOwnership.customBillsIncluded)
        }

        @Test
        fun `updateRentIncludesBills throws exception when initialLastModifiedDate does not match current lastModifiedDate`() {
            // Arrange
            val propertyOwnership =
                MockLandlordData.createOccupiedPropertyOwnership()
            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
                propertyOwnership,
            )

            // Act & Assert
            val exception =
                assertThrows<UpdateConflictException> {
                    propertyOwnershipService.updateRentIncludesBills(
                        propertyOwnership.id,
                        billsIncludedList = "GAS,BROADBAND,SOMETHING_ELSE",
                        customBillsIncluded = "Dog sitting",
                        initialLastModifiedDate = propertyOwnership.getMostRecentlyUpdated().minus(1, ChronoUnit.MINUTES),
                    )
                }

            assertEquals(
                "The property ownership record has been updated since this update session started.",
                exception.message,
            )
        }
    }

    @Nested
    inner class UpdateFurnishedStatus {
        @Test
        fun `updateFurnishedStatus updates the property's furnished status`() {
            // Arrange
            val propertyOwnership =
                MockLandlordData.createOccupiedPropertyOwnership(
                    id = 1,
                    furnishedStatus = FurnishedStatus.FURNISHED,
                )
            val newFurnishedStatus = FurnishedStatus.PART_FURNISHED
            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
                propertyOwnership,
            )

            // Act
            propertyOwnershipService.updateFurnishedStatus(
                propertyOwnership.id,
                furnishedStatus = newFurnishedStatus,
                initialLastModifiedDate = propertyOwnership.getMostRecentlyUpdated(),
            )

            // Assert
            assertEquals(newFurnishedStatus, propertyOwnership.furnishedStatus)
        }

        @Test
        fun `updateFurnishedStatus throws exception when initialLastModifiedDate does not match current lastModifiedDate`() {
            // Arrange
            val propertyOwnership =
                MockLandlordData.createOccupiedPropertyOwnership()
            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
                propertyOwnership,
            )

            // Act & Assert
            val exception =
                assertThrows<UpdateConflictException> {
                    propertyOwnershipService.updateFurnishedStatus(
                        propertyOwnership.id,
                        furnishedStatus = FurnishedStatus.UNFURNISHED,
                        initialLastModifiedDate = propertyOwnership.getMostRecentlyUpdated().minus(1, ChronoUnit.MINUTES),
                    )
                }

            assertEquals(
                "The property ownership record has been updated since this update session started.",
                exception.message,
            )
        }
    }

    @Nested
    inner class UpdateRentFrequencyAndAmount {
        @Test
        fun `updateRentFrequencyAndAmount updates the property's rent frequency and amount`() {
            // Arrange
            val propertyOwnership =
                MockLandlordData.createOccupiedPropertyOwnership(
                    id = 1,
                    rentFrequency = RentFrequency.MONTHLY,
                    customRentFrequency = null,
                    rentAmount = BigDecimal(100),
                )
            val newRentFrequency = RentFrequency.OTHER
            val newCustomRentFrequency = "Every 5 days"
            val newRentAmount = BigDecimal(50)
            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
                propertyOwnership,
            )

            // Act
            propertyOwnershipService.updateRentFrequencyAndAmount(
                propertyOwnership.id,
                rentFrequency = newRentFrequency,
                customRentFrequency = newCustomRentFrequency,
                rentAmount = newRentAmount,
                initialLastModifiedDate = propertyOwnership.getMostRecentlyUpdated(),
            )

            // Assert
            assertEquals(newRentFrequency, propertyOwnership.rentFrequency)
            assertEquals(newCustomRentFrequency, propertyOwnership.customRentFrequency)
            assertEquals(newRentAmount, propertyOwnership.rentAmount)
        }

        @Test
        fun `updateRentFrequencyAndAmount throws exception if initialLastModifiedDate does not match current lastModifiedDate`() {
            // Arrange
            val propertyOwnership =
                MockLandlordData.createOccupiedPropertyOwnership()
            whenever(mockPropertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnership.id)).thenReturn(
                propertyOwnership,
            )

            // Act & Assert
            val exception =
                assertThrows<UpdateConflictException> {
                    propertyOwnershipService.updateRentFrequencyAndAmount(
                        propertyOwnership.id,
                        rentFrequency = RentFrequency.WEEKLY,
                        customRentFrequency = null,
                        rentAmount = BigDecimal(120),
                        initialLastModifiedDate = propertyOwnership.getMostRecentlyUpdated().minus(1, ChronoUnit.MINUTES),
                    )
                }

            assertEquals(
                "The property ownership record has been updated since this update session started.",
                exception.message,
            )
        }
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

    @Nested
    inner class GetIncompleteCompliancesForLandlord {
        private val principalName = "principalName"

        @Test
        fun `getIncompleteCompliancesForLandlord returns occupied properties without completed compliance`() {
            // Arrange
            val occupiedPropertyWithoutCompliance =
                MockLandlordData.createOccupiedPropertyOwnership(currentNumTenants = 1)
            val occupiedPropertyWithCompliance =
                MockLandlordData.createOccupiedPropertyOwnership(currentNumTenants = 1, id = 2)
            ReflectionTestUtils.setField(
                occupiedPropertyWithCompliance,
                "propertyCompliance",
                mock<PropertyCompliance>(),
            )
            val unoccupiedProperty = MockLandlordData.createPropertyOwnership(currentNumTenants = 0)
            val properties =
                listOf(
                    unoccupiedProperty,
                    occupiedPropertyWithCompliance,
                    occupiedPropertyWithoutCompliance,
                )

            whenever(
                mockPropertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrue(principalName),
            ).thenReturn(properties)

            // Act
            val returnedIncompleteCompliances = propertyOwnershipService.getIncompleteCompliancesForLandlord(principalName)

            // Assert
            val expectedIncompleteCompliances =
                listOf(ComplianceStatusDataModel.fromPropertyOwnershipWithoutCompliance(occupiedPropertyWithoutCompliance))
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
        fun `returns the number of occupied properties without completed compliance`() {
            // Arrange
            val occupiedPropertyWithoutCompliance =
                MockLandlordData.createOccupiedPropertyOwnership(currentNumTenants = 1)
            val occupiedPropertyWithCompliance =
                MockLandlordData.createOccupiedPropertyOwnership(currentNumTenants = 1, id = 2)
            ReflectionTestUtils.setField(
                occupiedPropertyWithCompliance,
                "propertyCompliance",
                mock<PropertyCompliance>(),
            )
            val unoccupiedProperty = MockLandlordData.createPropertyOwnership(currentNumTenants = 0)

            whenever(
                mockPropertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrue(principalName),
            ).thenReturn(listOf(unoccupiedProperty, occupiedPropertyWithCompliance, occupiedPropertyWithoutCompliance))

            // Act
            val numberOfIncompleteCompliances = propertyOwnershipService.getNumberOfIncompleteCompliancesForLandlord(principalName)

            // Assert
            assertEquals(1, numberOfIncompleteCompliances)
        }

        @Test
        fun `returns 0 if there are no incomplete compliances for a landlord`() {
            // Arrange
            whenever(
                mockPropertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrue(principalName),
            ).thenReturn(emptyList())

            // Act
            val numberOfIncompleteCompliances = propertyOwnershipService.getNumberOfIncompleteCompliancesForLandlord(principalName)

            // Assert
            assertEquals(0, numberOfIncompleteCompliances)
        }
    }
}
