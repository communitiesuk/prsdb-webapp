package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OccupancyType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.Property
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@ExtendWith(MockitoExtension::class)
class PropertyRegistrationServiceTests {
    @Mock
    private lateinit var propertyRepository: PropertyRepository

    @Mock
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Mock
    private lateinit var addressDataService: AddressDataService

    @Mock
    private lateinit var mockLandlordRepository: LandlordRepository

    @Mock
    private lateinit var propertyService: PropertyService

    @Mock
    private lateinit var licenceService: LicenseService

    @Mock
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @InjectMocks
    private lateinit var propertyRegistrationService: PropertyRegistrationService

    @Test
    fun `getAddressIsRegistered returns false if no matching property is found`() {
        whenever(addressDataService.getCachedAddressRegisteredResult(any())).thenReturn(null)
        val uprn = 123456.toLong()
        whenever(propertyRepository.findByAddress_Uprn(uprn)).thenReturn(null)

        assertFalse(propertyRegistrationService.getIsAddressRegistered(uprn))
    }

    @Test
    fun `getAddressIsRegistered returns the cached result if it exists`() {
        whenever(addressDataService.getCachedAddressRegisteredResult(any())).thenReturn(null)
        val uprn = 123456.toLong()
        whenever(addressDataService.getCachedAddressRegisteredResult(uprn)).thenReturn(true)

        assertTrue(propertyRegistrationService.getIsAddressRegistered(uprn))
        verify(propertyRepository, times(0)).findByAddress_Uprn(any())
        verify(propertyOwnershipRepository, times(0)).findByIsActiveTrueAndProperty_Id(any())
    }

    @Test
    fun `getAddressIsRegistered caches and returns false if the property is inactive`() {
        whenever(addressDataService.getCachedAddressRegisteredResult(any())).thenReturn(null)
        val uprn = 123456.toLong()
        val address =
            Address(
                AddressDataModel(
                    singleLineAddress = "1 Street Name, City, AB1 2CD",
                    uprn = uprn,
                ),
            )
        val propertyId = 789.toLong()

        val property = Property(id = propertyId, address = address, isActive = false)
        whenever(propertyRepository.findByAddress_Uprn(uprn)).thenReturn(property)

        assertFalse(propertyRegistrationService.getIsAddressRegistered(uprn))
        verify(addressDataService).setCachedAddressRegisteredResult(uprn, false)
    }

    @Test
    fun `getAddressIsRegistered caches and returns false if the active property has no active ownerships`() {
        whenever(addressDataService.getCachedAddressRegisteredResult(any())).thenReturn(null)
        val uprn = 123456.toLong()
        val address =
            Address(
                AddressDataModel(
                    singleLineAddress = "1 Street Name, City, AB1 2CD",
                    uprn = uprn,
                ),
            )
        val propertyId = 789.toLong()

        val property = Property(id = propertyId, address = address, isActive = true)
        whenever(propertyRepository.findByAddress_Uprn(uprn)).thenReturn(property)

        whenever(propertyOwnershipRepository.findByIsActiveTrueAndProperty_Id(propertyId)).thenReturn(null)

        assertFalse(propertyRegistrationService.getIsAddressRegistered(uprn))
        verify(addressDataService).setCachedAddressRegisteredResult(uprn, false)
    }

    @Test
    fun `getAddressIsRegistered caches and returns true if the active property has one active ownership`() {
        whenever(addressDataService.getCachedAddressRegisteredResult(any())).thenReturn(null)
        val uprn = 123456.toLong()
        val address =
            Address(
                AddressDataModel(
                    singleLineAddress = "1 Street Name, City, AB1 2CD",
                    uprn = uprn,
                ),
            )
        val propertyId = 789.toLong()

        val property = Property(id = propertyId, address = address, isActive = true)
        whenever(propertyRepository.findByAddress_Uprn(uprn)).thenReturn(property)

        val activeOwnership = PropertyOwnership(id = 456.toLong(), isActive = true)
        whenever(propertyOwnershipRepository.findByIsActiveTrueAndProperty_Id(propertyId)).thenReturn(activeOwnership)

        assertTrue(propertyRegistrationService.getIsAddressRegistered(uprn))
        verify(addressDataService).setCachedAddressRegisteredResult(uprn, true)
    }

    @Test
    fun `registerProperty throws an error if the logged in user is not a landlord`() {
        // TODO: PRSD-729
    }

    @Test
    fun `registerProperty registers the property if all fields are populated`() {
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val propertyType = PropertyType.DETACHED_HOUSE
        val licenceType = LicensingType.SELECTIVE_LICENCE
        val licenceNumber = "L1234"
        val landlordType = LandlordType.SOLE
        val ownershipType = OwnershipType.FREEHOLD
        val numberOfHouseholds = 1
        val numberOfPeople = 2
        val baseUserId = "landlord-user"
        val landlord = Landlord()
        val property = Property()
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)
        val expectedPropertyOwnershipId = 1234.toLong()
        val licence = License()

        val expectedPropertyOwnership =
            PropertyOwnership(
                id = expectedPropertyOwnershipId,
                isActive = true,
                occupancyType = OccupancyType.SINGLE_FAMILY_DWELLING,
                landlordType = landlordType,
                ownershipType = ownershipType,
                currentNumHouseholds = numberOfHouseholds,
                currentNumTenants = numberOfPeople,
                registrationNumber = registrationNumber,
                primaryLandlord = landlord,
                property = property,
                license = licence,
            )

        whenever(mockLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(landlord)
        whenever(propertyService.createProperty(addressDataModel, propertyType)).thenReturn(
            property,
        )
        whenever(licenceService.createLicense(licenceType, licenceNumber)).thenReturn(licence)
        whenever(
            propertyOwnershipService.createPropertyOwnership(
                landlordType = landlordType,
                ownershipType = ownershipType,
                numberOfHouseholds = numberOfHouseholds,
                numberOfPeople = numberOfPeople,
                primaryLandlord = landlord,
                property = property,
                license = licence,
            ),
        ).thenReturn(expectedPropertyOwnership)

        val propertyId =
            propertyRegistrationService.registerProperty(
                addressDataModel,
                propertyType,
                licenceType,
                licenceNumber,
                landlordType,
                ownershipType,
                numberOfHouseholds,
                numberOfPeople,
                baseUserId,
            )

        assertEquals(expectedPropertyOwnershipId, propertyId)
    }

    @Test
    fun `registerProperty registers the property if there is no license`() {
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val propertyType = PropertyType.DETACHED_HOUSE
        val licenceType = LicensingType.NO_LICENSING
        val licenceNumber = ""
        val landlordType = LandlordType.SOLE
        val ownershipType = OwnershipType.FREEHOLD
        val numberOfHouseholds = 1
        val numberOfPeople = 2
        val baseUserId = "landlord-user"
        val landlord = Landlord()
        val property = Property()
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)
        val expectedPropertyOwnershipId = 1234.toLong()

        val expectedPropertyOwnership =
            PropertyOwnership(
                id = expectedPropertyOwnershipId,
                isActive = true,
                occupancyType = OccupancyType.SINGLE_FAMILY_DWELLING,
                landlordType = landlordType,
                ownershipType = ownershipType,
                currentNumHouseholds = numberOfHouseholds,
                currentNumTenants = numberOfPeople,
                registrationNumber = registrationNumber,
                primaryLandlord = landlord,
                property = property,
                license = null,
            )

        whenever(mockLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(landlord)
        whenever(propertyService.createProperty(addressDataModel, propertyType)).thenReturn(
            property,
        )
        whenever(
            propertyOwnershipService.createPropertyOwnership(
                landlordType = landlordType,
                ownershipType = ownershipType,
                numberOfHouseholds = numberOfHouseholds,
                numberOfPeople = numberOfPeople,
                primaryLandlord = landlord,
                property = property,
                license = null,
            ),
        ).thenReturn(expectedPropertyOwnership)

        val propertyId =
            propertyRegistrationService.registerProperty(
                addressDataModel,
                propertyType,
                licenceType,
                licenceNumber,
                landlordType,
                ownershipType,
                numberOfHouseholds,
                numberOfPeople,
                baseUserId,
            )

        assertEquals(expectedPropertyOwnershipId, propertyId)
    }
}
