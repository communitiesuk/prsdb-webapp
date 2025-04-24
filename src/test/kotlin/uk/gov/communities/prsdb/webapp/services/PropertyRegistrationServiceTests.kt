package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.Property
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership

@ExtendWith(MockitoExtension::class)
class PropertyRegistrationServiceTests {
    @Mock
    private lateinit var mockPropertyRepository: PropertyRepository

    @Mock
    private lateinit var mockPropertyOwnershipRepository: PropertyOwnershipRepository

    @Mock
    private lateinit var mockLandlordRepository: LandlordRepository

    @Mock
    private lateinit var mockFormContextRepository: FormContextRepository

    @Mock
    private lateinit var mockRegisteredAddressCache: RegisteredAddressCache

    @Mock
    private lateinit var mockPropertyService: PropertyService

    @Mock
    private lateinit var mockLicenceService: LicenseService

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockSession: HttpSession

    @InjectMocks
    private lateinit var propertyRegistrationService: PropertyRegistrationService

    @ParameterizedTest
    @CsvSource("true", "false")
    fun `getIsAddressRegistered returns the expected value when the given uprn is cached`(expectedValue: Boolean) {
        val uprn = 0L

        whenever(mockRegisteredAddressCache.getCachedAddressRegisteredResult(uprn)).thenReturn(expectedValue)

        val result = propertyRegistrationService.getIsAddressRegistered(uprn)

        assertEquals(expectedValue, result)
    }

    @Test
    fun `getIsAddressRegistered caches and returns false when there's no property associated with the given uprn`() {
        val uprn = 0L

        whenever(mockRegisteredAddressCache.getCachedAddressRegisteredResult(uprn)).thenReturn(null)
        whenever(mockPropertyRepository.findByAddress_Uprn(uprn)).thenReturn(null)

        val result = propertyRegistrationService.getIsAddressRegistered(uprn)

        verify(mockRegisteredAddressCache).setCachedAddressRegisteredResult(uprn, false)
        assertFalse(result)
    }

    @Test
    fun `getIsAddressRegistered caches and returns false the property associated with the given uprn is inactive`() {
        val uprn = 0L
        val inactiveProperty = Property()

        whenever(mockRegisteredAddressCache.getCachedAddressRegisteredResult(uprn)).thenReturn(null)
        whenever(mockPropertyRepository.findByAddress_Uprn(uprn)).thenReturn(inactiveProperty)

        val result = propertyRegistrationService.getIsAddressRegistered(uprn)

        verify(mockRegisteredAddressCache).setCachedAddressRegisteredResult(uprn, false)
        assertFalse(result)
    }

    @ParameterizedTest
    @CsvSource("true", "false")
    fun `getAddressIsRegistered caches and returns the expected value when the given uprn is not cached or associated with a property`(
        expectedValue: Boolean,
    ) {
        val uprn = 0L
        val activeProperty = Property(id = 1, address = Address(), isActive = true)

        whenever(mockRegisteredAddressCache.getCachedAddressRegisteredResult(uprn)).thenReturn(null)
        whenever(mockPropertyRepository.findByAddress_Uprn(uprn)).thenReturn(activeProperty)
        whenever(mockPropertyOwnershipRepository.existsByIsActiveTrueAndProperty_Id(activeProperty.id))
            .thenReturn(expectedValue)

        val result = propertyRegistrationService.getIsAddressRegistered(uprn)

        verify(mockRegisteredAddressCache).setCachedAddressRegisteredResult(uprn, result)
        assertEquals(expectedValue, result)
    }

    @Test
    fun `getAddressIsRegistered ignores the cache when ignoreCache is true`() {
        val expectedValue = true

        val uprn = 0L
        val activeProperty = Property(id = 1, address = Address(), isActive = true)

        whenever(mockPropertyRepository.findByAddress_Uprn(uprn)).thenReturn(activeProperty)
        whenever(mockPropertyOwnershipRepository.existsByIsActiveTrueAndProperty_Id(activeProperty.id))
            .thenReturn(expectedValue)

        val result = propertyRegistrationService.getIsAddressRegistered(uprn, ignoreCache = true)

        verify(mockRegisteredAddressCache, never()).getCachedAddressRegisteredResult(uprn)
        assertEquals(expectedValue, result)
    }

    @Test
    fun `registerPropertyAndReturnPropertyRegistrationNumber throws an error if the given address is registered`() {
        val registeredAddress = AddressDataModel(singleLineAddress = "1 Example Road", uprn = 0L)

        val spiedOnPropertyRegistrationService = spy(propertyRegistrationService)
        whenever(
            spiedOnPropertyRegistrationService.getIsAddressRegistered(registeredAddress.uprn!!, ignoreCache = true),
        ).thenReturn(true)

        val errorThrown =
            assertThrows<EntityExistsException> {
                spiedOnPropertyRegistrationService.registerPropertyAndReturnPropertyRegistrationNumber(
                    registeredAddress,
                    PropertyType.DETACHED_HOUSE,
                    LicensingType.NO_LICENSING,
                    "license number",
                    OwnershipType.FREEHOLD,
                    1,
                    1,
                    "baseUserId",
                )
            }

        assertEquals("Address already registered", errorThrown.message)
    }

    @Test
    fun `registerPropertyAndReturnPropertyRegistrationNumber throws an error if the logged in user is not a landlord`() {
        val nonLandlordUserId = "baseUserId"

        whenever(mockLandlordRepository.findByBaseUser_Id(nonLandlordUserId)).thenReturn(null)

        val errorThrown =
            assertThrows<EntityNotFoundException> {
                propertyRegistrationService.registerPropertyAndReturnPropertyRegistrationNumber(
                    AddressDataModel("1 Example Road"),
                    PropertyType.DETACHED_HOUSE,
                    LicensingType.NO_LICENSING,
                    "license number",
                    OwnershipType.FREEHOLD,
                    1,
                    1,
                    nonLandlordUserId,
                )
            }

        assertEquals("User not registered as a landlord", errorThrown.message)
    }

    @Test
    fun `registerPropertyAndReturnPropertyRegistrationNumber registers the property if all fields are populated`() {
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val propertyType = PropertyType.DETACHED_HOUSE
        val licenceType = LicensingType.SELECTIVE_LICENCE
        val licenceNumber = "L1234"
        val ownershipType = OwnershipType.FREEHOLD
        val numberOfHouseholds = 1
        val numberOfPeople = 2
        val baseUserId = "landlord-user"
        val landlord = Landlord()
        val property = Property()
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)
        val licence = License(licenceType, licenceNumber)

        val expectedPropertyOwnership =
            createPropertyOwnership(
                ownershipType = ownershipType,
                currentNumHouseholds = numberOfHouseholds,
                currentNumTenants = numberOfPeople,
                primaryLandlord = landlord,
                property = property,
                license = licence,
                registrationNumber = registrationNumber,
            )

        whenever(mockLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(landlord)
        whenever(mockPropertyService.activateOrCreateProperty(addressDataModel, propertyType)).thenReturn(
            property,
        )
        whenever(mockLicenceService.createLicense(licenceType, licenceNumber)).thenReturn(licence)
        whenever(
            mockPropertyOwnershipService.createPropertyOwnership(
                ownershipType = ownershipType,
                numberOfHouseholds = numberOfHouseholds,
                numberOfPeople = numberOfPeople,
                primaryLandlord = landlord,
                property = property,
                license = licence,
            ),
        ).thenReturn(expectedPropertyOwnership)

        val propertyRegistrationNumber =
            propertyRegistrationService.registerPropertyAndReturnPropertyRegistrationNumber(
                addressDataModel,
                propertyType,
                licenceType,
                licenceNumber,
                ownershipType,
                numberOfHouseholds,
                numberOfPeople,
                baseUserId,
            )

        assertEquals(expectedPropertyOwnership.registrationNumber, propertyRegistrationNumber)
    }

    @Test
    fun `registerPropertyAndReturnPropertyRegistrationNumber registers the property if there is no license`() {
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val propertyType = PropertyType.DETACHED_HOUSE
        val licenceType = LicensingType.NO_LICENSING
        val licenceNumber = ""
        val ownershipType = OwnershipType.FREEHOLD
        val numberOfHouseholds = 1
        val numberOfPeople = 2
        val baseUserId = "landlord-user"
        val landlord = Landlord()
        val property = Property()
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)

        val expectedPropertyOwnership =
            createPropertyOwnership(
                ownershipType = ownershipType,
                currentNumHouseholds = numberOfHouseholds,
                currentNumTenants = numberOfPeople,
                primaryLandlord = landlord,
                property = property,
                license = null,
                registrationNumber = registrationNumber,
            )

        whenever(mockLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(landlord)
        whenever(mockPropertyService.activateOrCreateProperty(addressDataModel, propertyType)).thenReturn(
            property,
        )
        whenever(
            mockPropertyOwnershipService.createPropertyOwnership(
                ownershipType = ownershipType,
                numberOfHouseholds = numberOfHouseholds,
                numberOfPeople = numberOfPeople,
                primaryLandlord = landlord,
                property = property,
                license = null,
            ),
        ).thenReturn(expectedPropertyOwnership)

        val propertyRegistrationNumber =
            propertyRegistrationService.registerPropertyAndReturnPropertyRegistrationNumber(
                addressDataModel,
                propertyType,
                licenceType,
                licenceNumber,
                ownershipType,
                numberOfHouseholds,
                numberOfPeople,
                baseUserId,
            )

        assertEquals(expectedPropertyOwnership.registrationNumber, propertyRegistrationNumber)
    }

    @Test
    fun `getNumberOfIncompletePropertyRegistrationsForLandlord returns number of incomplete properties`() {
        val principalName = "principalName"
        val expectedIncompleteProperties = 3
        whenever(
            mockFormContextRepository.countFormContextsByUser_IdAndJourneyType(principalName, JourneyType.PROPERTY_REGISTRATION),
        ).thenReturn(expectedIncompleteProperties)

        val incompleteProperties = propertyRegistrationService.getNumberOfIncompletePropertyRegistrationsForLandlord(principalName)

        assertEquals(expectedIncompleteProperties, incompleteProperties)
    }

    @Test
    fun `getNumberOfIncompletePropertyRegistrationsForLandlord returns null if there are no incomplete properties`() {
        val principalName = "principalName"
        whenever(
            mockFormContextRepository.countFormContextsByUser_IdAndJourneyType(principalName, JourneyType.PROPERTY_REGISTRATION),
        ).thenReturn(0)

        val incompleteProperties = propertyRegistrationService.getNumberOfIncompletePropertyRegistrationsForLandlord(principalName)

        assertNull(incompleteProperties)
    }
}
