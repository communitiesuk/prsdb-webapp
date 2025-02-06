package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData.Companion.createAddress
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData.Companion.createLandlord
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData.Companion.createProperty
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData.Companion.createPropertyOwnership
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
        whenever(mockPropertyOwnershipRepository.save(any(PropertyOwnership::class.java))).thenReturn(
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
        whenever(mockPropertyOwnershipRepository.save(any(PropertyOwnership::class.java))).thenReturn(
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
        private val landlord = createLandlord()

        private val address1 = "11 Example Road, EG1 2AB"
        private val address2 = "12 Example Road, EG1 2AB"
        private val localAuthority = LocalAuthority(11, "DERBYSHIRE DALES DISTRICT COUNCIL", "1045")
        private val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)

        private val property1 = createProperty(address = createAddress(address1, localAuthority))
        private val property2 = createProperty(address = createAddress(address2, localAuthority))

        private val expectedLocalAuthority = localAuthority.name
        private val expectedRegistrationNumber =
            RegistrationNumberDataModel
                .fromRegistrationNumber(
                    registrationNumber,
                ).toString()
        private val expectedPropertyLicence = "Not Licenced"
        private val expectedIsTenantedMessageKey = "commonText.no"

        private val propertyOwnership1 =
            createPropertyOwnership(
                primaryLandlord = landlord,
                property = property1,
                registrationNumber = registrationNumber,
                license = null,
                currentNumTenants = 0,
            )
        private val propertyOwnership2 =
            createPropertyOwnership(
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
        val prnMatchingPropertyOwnership = listOf(createPropertyOwnership())
        val expectedSearchResults =
            prnMatchingPropertyOwnership.map { PropertySearchResultViewModel.fromPropertyOwnership(it) }

        whenever(mockPropertyOwnershipRepository.searchMatchingPRN(searchPRN.number)).thenReturn(
            prnMatchingPropertyOwnership,
        )

        val searchResults = propertyOwnershipService.searchForProperties(searchPRN.toString())

        assertEquals(expectedSearchResults, searchResults)
    }

    @Test
    fun `searchForProperties returns an exact UPRN match then a collection of fuzzy matches when the search term is a UPRN`() {
        val searchUPRN = "123"

        val uprnMatchingPropertyOwnership =
            listOf(createPropertyOwnership(property = createProperty(address = createAddress(uprn = searchUPRN.toLong()))))
        val fuzzyMatchingPropertyOwnerships = listOf(createPropertyOwnership(), createPropertyOwnership())

        val expectedPropertyOwnerships = uprnMatchingPropertyOwnership + fuzzyMatchingPropertyOwnerships
        val expectedSearchResults =
            expectedPropertyOwnerships.map { PropertySearchResultViewModel.fromPropertyOwnership(it) }

        whenever(mockPropertyOwnershipRepository.searchMatchingUPRN(searchUPRN.toLong())).thenReturn(
            uprnMatchingPropertyOwnership,
        )
        whenever(mockPropertyOwnershipRepository.searchMatching(searchUPRN)).thenReturn(
            fuzzyMatchingPropertyOwnerships,
        )

        val searchResults = propertyOwnershipService.searchForProperties(searchUPRN)

        assertEquals(expectedSearchResults, searchResults)
    }

    @Test
    fun `searchForProperties returns a collection of fuzzy matches when the search term is not a PRN or UPRN`() {
        val searchTerm = "EG1 2AB"

        val expectedPropertyOwnerships = listOf(createPropertyOwnership(), createPropertyOwnership())
        val expectedSearchResults =
            expectedPropertyOwnerships.map { PropertySearchResultViewModel.fromPropertyOwnership(it) }

        whenever(mockPropertyOwnershipRepository.searchMatching(searchTerm)).thenReturn(
            expectedPropertyOwnerships,
        )

        val searchResults = propertyOwnershipService.searchForProperties(searchTerm)

        assertEquals(expectedSearchResults, searchResults)
    }
}
