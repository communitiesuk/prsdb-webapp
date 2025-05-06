package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpSession
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
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
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
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
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createFormContext
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
    private lateinit var localAuthorityService: LocalAuthorityService

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

    @Nested
    inner class IncompleteProperties {
        private lateinit var currentDate: LocalDate
        private lateinit var currentInstant: Instant

        @BeforeEach
        fun setup() {
            currentDate = DateTimeHelper().getCurrentDateInUK()
            currentInstant =
                LocalDateTime(
                    currentDate.year,
                    currentDate.monthNumber,
                    currentDate.dayOfMonth,
                    11,
                    30,
                ).toInstant(TimeZone.of("Europe/London"))
        }

        @Test
        fun `getNumberOfIncompletePropertyRegistrationsForLandlord returns number of valid incomplete properties`() {
            val createdTodayDate = currentInstant.toJavaInstant()
            val createdYesterdayDate = currentInstant.minus(1, DateTimeUnit.DAY, TimeZone.of("Europe/London")).toJavaInstant()
            val outOfDateCreatedDate = currentInstant.minus(29, DateTimeUnit.DAY, TimeZone.of("Europe/London")).toJavaInstant()

            val principalName = "principalName"
            val incompleteProperties =
                listOf(
                    createFormContext(createdDate = createdTodayDate),
                    createFormContext(createdDate = createdYesterdayDate),
                    createFormContext(createdDate = outOfDateCreatedDate),
                )

            val expectedIncompletePropertiesNumber = 2

            whenever(
                mockFormContextRepository.findAllByUser_IdAndJourneyType(principalName, JourneyType.PROPERTY_REGISTRATION),
            ).thenReturn(incompleteProperties)

            val incompletePropertiesNumber =
                propertyRegistrationService.getNumberOfIncompletePropertyRegistrationsForLandlord(
                    principalName,
                )

            assertEquals(expectedIncompletePropertiesNumber, incompletePropertiesNumber)
        }

        @Test
        fun `getNumberOfIncompletePropertyRegistrationsForLandlord returns 0 if there are no incomplete properties`() {
            val principalName = "principalName"
            val expectedNumberOfIncompleteProperties = 0
            whenever(
                mockFormContextRepository.findAllByUser_IdAndJourneyType(principalName, JourneyType.PROPERTY_REGISTRATION),
            ).thenReturn(emptyList())

            val incompleteProperties = propertyRegistrationService.getNumberOfIncompletePropertyRegistrationsForLandlord(principalName)

            assertEquals(expectedNumberOfIncompleteProperties, incompleteProperties)
        }

        @Test
        fun `getIncompletePropertyForLandlord returns the form context for a valid incomplete property`() {
            val createdTodayDate = currentInstant.toJavaInstant()

            val expectedFormContext = createFormContext(createdDate = createdTodayDate)
            val principalName = "user"

            whenever(
                mockFormContextRepository.findByIdAndUser_IdAndJourneyType(
                    expectedFormContext.id,
                    principalName,
                    JourneyType.PROPERTY_REGISTRATION,
                ),
            ).thenReturn(expectedFormContext)

            val formContext =
                propertyRegistrationService.getIncompletePropertyForLandlord(expectedFormContext.id, principalName)

            assertEquals(expectedFormContext, formContext)
        }

        @Test
        fun `getIncompletePropertyForLandlord returns NOT_FOUND error for an invalid incomplete property`() {
            val formContextId: Long = 123
            val principalName = "user"

            val expectedErrorMessage =
                "404 NOT_FOUND \"Form context with ID: $formContextId and journey type: " +
                    "${JourneyType.PROPERTY_REGISTRATION.name} not found for base user: $principalName\""

            whenever(
                mockFormContextRepository.findByIdAndUser_IdAndJourneyType(formContextId, principalName, JourneyType.PROPERTY_REGISTRATION),
            ).thenReturn(null)

            // Act and Assert
            val exception =
                assertThrows<ResponseStatusException> {
                    propertyRegistrationService.getIncompletePropertyForLandlord(formContextId, principalName)
                }
            kotlin.test.assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
            kotlin.test.assertEquals(expectedErrorMessage, exception.message)
        }

        @Test
        fun `getIncompletePropertyForLandlord returns BAD_REQUEST error for an out of date incomplete property`() {
            val outOfDateCreatedDate = currentInstant.minus(29, DateTimeUnit.DAY, TimeZone.of("Europe/London")).toJavaInstant()

            val principalName = "user"
            val formContext = createFormContext(createdDate = outOfDateCreatedDate)

            val expectedErrorMessage = "400 BAD_REQUEST \"Completed date for orm context with ID: ${formContext.id} is in the past\""

            whenever(
                mockFormContextRepository.findByIdAndUser_IdAndJourneyType(
                    formContext.id,
                    principalName,
                    JourneyType.PROPERTY_REGISTRATION,
                ),
            ).thenReturn(formContext)

            // Act and Assert
            val exception =
                assertThrows<ResponseStatusException> {
                    propertyRegistrationService.getIncompletePropertyForLandlord(formContext.id, principalName)
                }
            kotlin.test.assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
            kotlin.test.assertEquals(expectedErrorMessage, exception.message)
        }
    }
}
