package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.OccupancyType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationStatus
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.Property
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.PropertySearchResultViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.RegisteredPropertyViewModel

@ExtendWith(MockitoExtension::class)
class PropertyOwnershipServiceTests {
    @Mock
    private lateinit var mockPropertyOwnershipRepository: PropertyOwnershipRepository

    @Mock
    private lateinit var mockRegistrationNumberService: RegistrationNumberService

    @Mock
    private lateinit var mockLandlordService: LandlordService

    @InjectMocks
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @Test
    fun `createPropertyOwnership creates a property ownership`() {
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)
        val landlordType = LandlordType.SOLE
        val ownershipType = OwnershipType.FREEHOLD
        val households = 1
        val tenants = 2
        val landlord = Landlord()
        val property = Property()
        val license = License()

        val expectedPropertyOwnership =
            PropertyOwnership(
                occupancyType = OccupancyType.SINGLE_FAMILY_DWELLING,
                landlordType = landlordType,
                ownershipType = ownershipType,
                currentNumHouseholds = households,
                currentNumTenants = tenants,
                registrationNumber = registrationNumber,
                primaryLandlord = landlord,
                property = property,
                license = license,
            )

        whenever(mockRegistrationNumberService.createRegistrationNumber(RegistrationNumberType.PROPERTY)).thenReturn(
            registrationNumber,
        )
        whenever(mockPropertyOwnershipRepository.save(any<PropertyOwnership>())).thenReturn(
            expectedPropertyOwnership,
        )

        propertyOwnershipService.createPropertyOwnership(
            landlordType,
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
        val landlordType = LandlordType.SOLE
        val ownershipType = OwnershipType.FREEHOLD
        val households = 1
        val tenants = 2
        val landlord = Landlord()
        val property = Property()

        val expectedPropertyOwnership =
            PropertyOwnership(
                occupancyType = OccupancyType.SINGLE_FAMILY_DWELLING,
                landlordType = landlordType,
                ownershipType = ownershipType,
                currentNumHouseholds = households,
                currentNumTenants = tenants,
                registrationNumber = registrationNumber,
                primaryLandlord = landlord,
                property = property,
                license = null,
            )

        whenever(mockRegistrationNumberService.createRegistrationNumber(RegistrationNumberType.PROPERTY)).thenReturn(
            registrationNumber,
        )
        whenever(mockPropertyOwnershipRepository.save(any<PropertyOwnership>())).thenReturn(
            expectedPropertyOwnership,
        )

        propertyOwnershipService.createPropertyOwnership(
            landlordType,
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

        private val address1 = "11 Example Road, EG1 2AB"
        private val address2 = "12 Example Road, EG1 2AB"
        private val localAuthority = LocalAuthority(11, "DERBYSHIRE DALES DISTRICT COUNCIL", "1045")
        private val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)

        private val property1 =
            MockLandlordData.createProperty(address = MockLandlordData.createAddress(address1, localAuthority))
        private val property2 =
            MockLandlordData.createProperty(address = MockLandlordData.createAddress(address2, localAuthority))

        private val expectedLocalAuthority = localAuthority.name
        private val expectedRegistrationNumber =
            RegistrationNumberDataModel.fromRegistrationNumber(registrationNumber).toString()
        private val expectedPropertyLicence = "Not Licenced"
        private val expectedIsTenantedMessageKey = "commonText.no"

        private val propertyOwnership1 =
            MockLandlordData.createPropertyOwnership(
                primaryLandlord = landlord,
                property = property1,
                registrationNumber = registrationNumber,
                license = null,
                currentNumTenants = 0,
            )
        private val propertyOwnership2 =
            MockLandlordData.createPropertyOwnership(
                primaryLandlord = landlord,
                property = property2,
                registrationNumber = registrationNumber,
                license = null,
                currentNumTenants = 0,
            )

        private val landlordsProperties: List<PropertyOwnership> =
            listOf(propertyOwnership1, propertyOwnership2)

        private val expectedResults: List<RegisteredPropertyViewModel> =
            listOf(
                RegisteredPropertyViewModel(
                    address1,
                    expectedRegistrationNumber,
                    expectedLocalAuthority,
                    expectedPropertyLicence,
                    expectedIsTenantedMessageKey,
                ),
                RegisteredPropertyViewModel(
                    address2,
                    expectedRegistrationNumber,
                    expectedLocalAuthority,
                    expectedPropertyLicence,
                    expectedIsTenantedMessageKey,
                ),
            )

        @Test
        fun `Returns a list of Landlords properties in correctly formatted data model from landlords BaseUser_Id`() {
            whenever(
                mockPropertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndProperty_Status(
                    "landlord",
                    RegistrationStatus.REGISTERED,
                ),
            ).thenReturn(landlordsProperties)

            val result = propertyOwnershipService.getRegisteredPropertiesForLandlord("landlord")

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

            val result = propertyOwnershipService.getRegisteredPropertiesForLandlord(landlord.id)

            assertTrue(result.size == 2)
            assertEquals(expectedResults, result)
        }
    }

    @Test
    fun `searchForProperties returns a single matching property when the search term is a PRN`() {
        val searchPRN = RegistrationNumberDataModel(RegistrationNumberType.PROPERTY, 123)
        val prnMatchingPropertyOwnership = listOf(MockLandlordData.createPropertyOwnership())
        val expectedSearchResults =
            prnMatchingPropertyOwnership.map { PropertySearchResultViewModel.fromPropertyOwnership(it) }

        whenever(mockPropertyOwnershipRepository.searchMatchingPRN(eq(searchPRN.number), pageable = any())).thenReturn(
            PageImpl(prnMatchingPropertyOwnership),
        )

        val searchResults = propertyOwnershipService.searchForProperties(searchPRN.toString())

        assertEquals(expectedSearchResults, searchResults.content)
    }

    @Test
    fun `searchForProperties returns no results when the search term is a non-property registration number`() {
        val nonPropertyRegNum = RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, 123)

        whenever(
            mockPropertyOwnershipRepository.searchMatching(eq(nonPropertyRegNum.toString()), pageable = any()),
        ).thenReturn(
            Page.empty(),
        )

        val searchResults = propertyOwnershipService.searchForProperties(nonPropertyRegNum.toString())

        verify(mockPropertyOwnershipRepository, never()).searchMatchingPRN(
            eq(nonPropertyRegNum.number),
            pageable = any(),
        )
        assertEquals(emptyList<PropertySearchResultViewModel>(), searchResults.content)
    }

    @Test
    fun `searchForProperties returns a single matching property when the search term is a UPRN`() {
        val searchUPRN = "123"

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
            uprnMatchingPropertyOwnership.map { PropertySearchResultViewModel.fromPropertyOwnership(it) }

        whenever(
            mockPropertyOwnershipRepository.searchMatchingUPRN(
                eq(searchUPRN.toLong()),
                pageable = any(),
            ),
        ).thenReturn(
            PageImpl(uprnMatchingPropertyOwnership),
        )

        val searchResults = propertyOwnershipService.searchForProperties(searchUPRN)

        assertEquals(expectedSearchResults, searchResults.content)
    }

    @Test
    fun `searchForProperties returns a collection of fuzzy matches when the search term is not a PRN or UPRN`() {
        val searchTerm = "EG1 2AB"

        val fuzzyMatchingPropertyOwnerships =
            listOf(MockLandlordData.createPropertyOwnership(), MockLandlordData.createPropertyOwnership())
        val expectedSearchResults =
            fuzzyMatchingPropertyOwnerships.map { PropertySearchResultViewModel.fromPropertyOwnership(it) }

        whenever(mockPropertyOwnershipRepository.searchMatching(eq(searchTerm), pageable = any())).thenReturn(
            PageImpl(fuzzyMatchingPropertyOwnerships),
        )

        val searchResults = propertyOwnershipService.searchForProperties(searchTerm)

        assertEquals(expectedSearchResults, searchResults.content)
    }

    @Test
    fun `searchForProperties returns the requested page of properties`() {
        val searchTerm = "searchTerm"
        val pageSize = 25
        val matchingProperties = (1..40).map { MockLandlordData.createPropertyOwnership() }

        val pageIndex1 = 0
        val pageRequest1 = PageRequest.of(pageIndex1, pageSize)
        val matchingPropertiesPage1 = matchingProperties.subList(0, pageSize)
        val expectedPage1SearchResults =
            matchingPropertiesPage1.map { PropertySearchResultViewModel.fromPropertyOwnership(it) }

        val pageIndex2 = 1
        val pageRequest2 = PageRequest.of(pageIndex2, pageSize)
        val matchingPropertiesPage2 = matchingProperties.subList(pageSize, matchingProperties.size)
        val expectedPage2SearchResults =
            matchingPropertiesPage2.map { PropertySearchResultViewModel.fromPropertyOwnership(it) }

        whenever(mockPropertyOwnershipRepository.searchMatching(searchTerm, pageable = pageRequest1))
            .thenReturn(PageImpl(matchingPropertiesPage1))
        whenever(mockPropertyOwnershipRepository.searchMatching(searchTerm, pageable = pageRequest2))
            .thenReturn(PageImpl(matchingPropertiesPage2))

        val searchResults1 = propertyOwnershipService.searchForProperties(searchTerm, pageIndex1)
        val searchResults2 = propertyOwnershipService.searchForProperties(searchTerm, pageIndex2)

        assertEquals(expectedPage1SearchResults, searchResults1.content)
        assertEquals(expectedPage2SearchResults, searchResults2.content)
    }
}
