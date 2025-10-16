package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpSession
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTY_FORM_CONTEXTS_DELETED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_NUMBER
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.NonStepJourneyDataKey
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.Property
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompletePropertiesDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI
import java.util.Optional

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
    private lateinit var mockLocalAuthorityService: LocalAuthorityService

    @Mock
    private lateinit var mockSession: HttpSession

    @Mock
    lateinit var confirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationEmail>

    @Mock
    lateinit var urlProvider: AbsoluteUrlProvider

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
    fun `registerProperty throws an error if the given address is registered`() {
        val registeredAddress = AddressDataModel(singleLineAddress = "1 Example Road", uprn = 0L)

        val spiedOnPropertyRegistrationService = spy(propertyRegistrationService)
        whenever(
            spiedOnPropertyRegistrationService.getIsAddressRegistered(registeredAddress.uprn!!, ignoreCache = true),
        ).thenReturn(true)

        val errorThrown =
            assertThrows<EntityExistsException> {
                spiedOnPropertyRegistrationService.registerProperty(
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
    fun `registerProperty throws an error if the logged in user is not a landlord`() {
        val nonLandlordUserId = "baseUserId"

        whenever(mockLandlordRepository.findByBaseUser_Id(nonLandlordUserId)).thenReturn(null)

        val errorThrown =
            assertThrows<EntityNotFoundException> {
                propertyRegistrationService.registerProperty(
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
    fun `registerProperty registers the property if all fields are populated`() {
        // Arrange
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val propertyType = PropertyType.DETACHED_HOUSE
        val licenceType = LicensingType.SELECTIVE_LICENCE
        val licenceNumber = "L1234"
        val ownershipType = OwnershipType.FREEHOLD
        val numberOfHouseholds = 1
        val numberOfPeople = 2
        val landlord = MockLandlordData.createLandlord()
        val property = Property()
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)
        val licence = License(licenceType, licenceNumber)

        val expectedPropertyOwnership =
            MockLandlordData.createPropertyOwnership(
                ownershipType = ownershipType,
                currentNumHouseholds = numberOfHouseholds,
                currentNumTenants = numberOfPeople,
                primaryLandlord = landlord,
                property = property,
                license = licence,
                registrationNumber = registrationNumber,
            )

        whenever(mockLandlordRepository.findByBaseUser_Id(landlord.baseUser.id)).thenReturn(landlord)
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
        whenever(urlProvider.buildLandlordDashboardUri()).thenReturn(URI("https:gov.uk"))

        // Act
        val propertyRegistrationNumber =
            propertyRegistrationService.registerProperty(
                addressDataModel,
                propertyType,
                licenceType,
                licenceNumber,
                ownershipType,
                numberOfHouseholds,
                numberOfPeople,
                landlord.baseUser.id,
            )

        // Assert
        assertEquals(expectedPropertyOwnership.registrationNumber, propertyRegistrationNumber)
        verify(mockPropertyOwnershipService).createPropertyOwnership(
            ownershipType = ownershipType,
            numberOfHouseholds = numberOfHouseholds,
            numberOfPeople = numberOfPeople,
            primaryLandlord = landlord,
            property = property,
            license = licence,
        )
    }

    @Test
    fun `registerProperty sends a confirmation email and caches the registration number when it registers the property`() {
        // Arrange
        val landlord = MockLandlordData.createLandlord()
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 5678)

        val expectedPropertyOwnership =
            MockLandlordData.createPropertyOwnership(
                primaryLandlord = landlord,
                registrationNumber = registrationNumber,
            )

        whenever(mockLandlordRepository.findByBaseUser_Id(any())).thenReturn(landlord)
        whenever(mockPropertyService.activateOrCreateProperty(any(), any()))
            .thenReturn(expectedPropertyOwnership.property)
        whenever(mockLicenceService.createLicense(any(), any())).thenReturn(expectedPropertyOwnership.license)
        whenever(
            mockPropertyOwnershipService.createPropertyOwnership(
                ownershipType = any(),
                numberOfHouseholds = any(),
                numberOfPeople = any(),
                primaryLandlord = any(),
                property = any(),
                license = anyOrNull(),
                isActive = any(),
                occupancyType = any(),
            ),
        ).thenReturn(expectedPropertyOwnership)

        val dashboardUri = URI("https:gov.uk")
        whenever(urlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)

        // Act
        propertyRegistrationService.registerProperty(
            AddressDataModel.fromAddress(expectedPropertyOwnership.property.address),
            PropertyType.DETACHED_HOUSE,
            LicensingType.SELECTIVE_LICENCE,
            "Licence",
            OwnershipType.FREEHOLD,
            2,
            3,
            "USER_ID",
        )

        // Assert
        verify(confirmationEmailSender).sendEmail(
            eq(landlord.email),
            argThat<PropertyRegistrationConfirmationEmail> { email ->
                email.prn == RegistrationNumberDataModel.fromRegistrationNumber(registrationNumber).toString()
                email.singleLineAddress == expectedPropertyOwnership.property.address.singleLineAddress &&
                    email.prsdUrl == dashboardUri.toString()
                email.isOccupied == (expectedPropertyOwnership.currentNumTenants > 0)
            },
        )

        verify(mockSession).setAttribute(eq(PROPERTY_REGISTRATION_NUMBER), eq(registrationNumber.number))
    }

    @Test
    fun `registerProperty registers the property if there is no license`() {
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val propertyType = PropertyType.DETACHED_HOUSE
        val licenceType = LicensingType.NO_LICENSING
        val licenceNumber = ""
        val ownershipType = OwnershipType.FREEHOLD
        val numberOfHouseholds = 1
        val numberOfPeople = 2
        val landlord = MockLandlordData.createLandlord()
        val property = Property()
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)

        val expectedPropertyOwnership =
            MockLandlordData.createPropertyOwnership(
                ownershipType = ownershipType,
                currentNumHouseholds = numberOfHouseholds,
                currentNumTenants = numberOfPeople,
                primaryLandlord = landlord,
                property = property,
                license = null,
                registrationNumber = registrationNumber,
            )

        whenever(mockLandlordRepository.findByBaseUser_Id(landlord.baseUser.id)).thenReturn(landlord)
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
        whenever(urlProvider.buildLandlordDashboardUri()).thenReturn(URI("https:gov.uk"))

        val propertyRegistrationNumber =
            propertyRegistrationService.registerProperty(
                addressDataModel,
                propertyType,
                licenceType,
                licenceNumber,
                ownershipType,
                numberOfHouseholds,
                numberOfPeople,
                landlord.baseUser.id,
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
                    MockLandlordData.createPropertyRegistrationFormContext(createdDate = createdTodayDate),
                    MockLandlordData.createPropertyRegistrationFormContext(createdDate = createdYesterdayDate),
                    MockLandlordData.createPropertyRegistrationFormContext(createdDate = outOfDateCreatedDate),
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
        fun `getIncompletePropertyFormContextForLandlordIfNotExpired returns the form context for a valid incomplete property`() {
            val createdTodayDate = currentInstant.toJavaInstant()

            val expectedFormContext = MockLandlordData.createPropertyRegistrationFormContext(createdDate = createdTodayDate)
            val principalName = "user"

            whenever(
                mockFormContextRepository.findByIdAndUser_IdAndJourneyType(
                    expectedFormContext.id,
                    principalName,
                    JourneyType.PROPERTY_REGISTRATION,
                ),
            ).thenReturn(expectedFormContext)

            val formContext =
                propertyRegistrationService.getIncompletePropertyFormContextForLandlordIfNotExpired(expectedFormContext.id, principalName)

            assertEquals(expectedFormContext, formContext)
        }

        @Test
        fun `getIncompletePropertyFormContextForLandlordIfNotExpired throws NOT_FOUND error when no FormContext is found`() {
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
                    propertyRegistrationService.getIncompletePropertyFormContextForLandlordIfNotExpired(formContextId, principalName)
                }
            assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
            assertEquals(expectedErrorMessage, exception.message)
        }

        @Test
        fun `getIncompletePropertyFormContextForLandlordIfNotExpired throws NOT_FOUND error for an out of date incomplete property`() {
            val outOfDateCreatedDate = currentInstant.minus(29, DateTimeUnit.DAY, TimeZone.of("Europe/London")).toJavaInstant()

            val principalName = "user"
            val formContext = MockLandlordData.createPropertyRegistrationFormContext(createdDate = outOfDateCreatedDate)

            val expectedErrorMessage = "404 NOT_FOUND \"Complete by date for form context with ID: ${formContext.id} is in the past\""

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
                    propertyRegistrationService.getIncompletePropertyFormContextForLandlordIfNotExpired(formContext.id, principalName)
                }
            assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
            assertEquals(expectedErrorMessage, exception.message)
        }

        @Test
        fun `getIncompletePropertiesForLandlord returns a list of valid incomplete properties`() {
            val address = "2, Example Road, EG"
            val context =
                "{\"lookup-address\":{\"houseNameOrNumber\":\"73\",\"postcode\":\"WC2R 1LA\"}," +
                    "\"${NonStepJourneyDataKey.LookedUpAddresses.key}\":\"[{\\\"singleLineAddress\\\":\\\"2, Example Road, EG\\\"," +
                    "\\\"localAuthorityId\\\":241,\\\"uprn\\\":2123456,\\\"buildingNumber\\\":\\\"2\\\"," +
                    "\\\"postcode\\\":\\\"EG\\\"}]\",\"select-address\":{\"address\":\"$address\"}}"

            val createdTodayDate = currentInstant.toJavaInstant()
            val formContextCreatedToday =
                MockLandlordData.createPropertyRegistrationFormContext(
                    id = 1,
                    createdDate = createdTodayDate,
                    context = context,
                )

            val createdYesterdayDate = currentInstant.minus(1, DateTimeUnit.DAY, TimeZone.of("Europe/London")).toJavaInstant()
            val formContextCreatedYesterday =
                MockLandlordData.createPropertyRegistrationFormContext(
                    id = 2,
                    createdDate = createdYesterdayDate,
                    context = context,
                )

            val outOfDateCreatedDate = currentInstant.minus(29, DateTimeUnit.DAY, TimeZone.of("Europe/London")).toJavaInstant()
            val formContextCreated29DaysAgo = MockLandlordData.createPropertyRegistrationFormContext(createdDate = outOfDateCreatedDate)

            val principalName = "principalName"
            val incompleteProperties = listOf(formContextCreatedToday, formContextCreatedYesterday, formContextCreated29DaysAgo)

            val createdTodayCompleteByDate = currentDate.plus(DatePeriod(days = 28))
            val createdYesterdayCompleteByDate = currentDate.plus(DatePeriod(days = 27))

            val expectedIncompletePropertiesList =
                listOf(
                    IncompletePropertiesDataModel(
                        formContextCreatedToday.id,
                        createdTodayCompleteByDate,
                        address,
                    ),
                    IncompletePropertiesDataModel(
                        formContextCreatedYesterday.id,
                        createdYesterdayCompleteByDate,
                        address,
                    ),
                )

            whenever(
                mockFormContextRepository.findAllByUser_IdAndJourneyType(principalName, JourneyType.PROPERTY_REGISTRATION),
            ).thenReturn(incompleteProperties)

            val incompletePropertiesList =
                propertyRegistrationService.getIncompletePropertiesForLandlord(
                    principalName,
                )

            assertEquals(expectedIncompletePropertiesList, incompletePropertiesList)
        }

        @Test
        fun `getIncompletePropertiesForLandlord returns an emptyList if there are no valid incomplete properties`() {
            val principalName = "principalName"
            val outOfDateCreatedDate = currentInstant.minus(29, DateTimeUnit.DAY, TimeZone.of("Europe/London")).toJavaInstant()

            val formContext = MockLandlordData.createPropertyRegistrationFormContext(createdDate = outOfDateCreatedDate)

            whenever(
                mockFormContextRepository.findAllByUser_IdAndJourneyType(principalName, JourneyType.PROPERTY_REGISTRATION),
            ).thenReturn(listOf(formContext))

            val incompleteProperties = propertyRegistrationService.getIncompletePropertiesForLandlord(principalName)

            assertTrue(incompleteProperties.isEmpty())
        }

        @Test
        fun `getIncompletePropertiesForLandlord returns an emptyList if there are no incomplete properties`() {
            val principalName = "principalName"
            whenever(
                mockFormContextRepository.findAllByUser_IdAndJourneyType(principalName, JourneyType.PROPERTY_REGISTRATION),
            ).thenReturn(emptyList())

            val incompleteProperties = propertyRegistrationService.getIncompletePropertiesForLandlord(principalName)

            assertTrue(incompleteProperties.isEmpty())
        }

        @Test
        fun `deleteIncompleteProperty deletes the form context for a valid incomplete property`() {
            val createdTodayDate = currentInstant.toJavaInstant()

            val formContext = MockLandlordData.createPropertyRegistrationFormContext(createdDate = createdTodayDate)
            val principalName = "user"

            whenever(
                mockFormContextRepository.findByIdAndUser_IdAndJourneyType(
                    formContext.id,
                    principalName,
                    JourneyType.PROPERTY_REGISTRATION,
                ),
            ).thenReturn(formContext)

            propertyRegistrationService.deleteIncompleteProperty(formContext.id, principalName)

            verify(mockFormContextRepository).delete(formContext)
        }

        @Test
        fun `deleteIncompleteProperty throws NOT_FOUND error for an invalid incomplete property`() {
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
                    propertyRegistrationService.deleteIncompleteProperty(formContextId, principalName)
                }
            assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
            assertEquals(expectedErrorMessage, exception.message)
        }

        @Test
        fun `addIncompletePropertyFormContextsDeletedThisSession adds a contextId to the list of cancelled formContexts this session`() {
            // Arrange
            whenever(mockSession.getAttribute(INCOMPLETE_PROPERTY_FORM_CONTEXTS_DELETED_THIS_SESSION))
                .thenReturn(listOf(1L))

            // Act
            propertyRegistrationService.addIncompletePropertyFormContextsDeletedThisSession(2L)

            // Assert
            verify(mockSession).setAttribute(
                INCOMPLETE_PROPERTY_FORM_CONTEXTS_DELETED_THIS_SESSION,
                listOf(1L, 2L),
            )
        }

        @Test
        fun `getIncompletePropertyFormContextsDeletedThisSession returns the list of contextIds deleted this session`() {
            // Arrange
            val expectedList = listOf(1L, 2L)
            whenever(mockSession.getAttribute(INCOMPLETE_PROPERTY_FORM_CONTEXTS_DELETED_THIS_SESSION))
                .thenReturn(expectedList)

            // Act
            val result = propertyRegistrationService.getIncompletePropertyFormContextsDeletedThisSession()

            // Assert
            assertEquals(expectedList, result)
        }

        @Test
        fun `getIncompletePropertyFormContextsDeletedThisSession returns an empty list if no contextIds have been deleted this session`() {
            // Arrange
            whenever(mockSession.getAttribute(INCOMPLETE_PROPERTY_FORM_CONTEXTS_DELETED_THIS_SESSION))
                .thenReturn(null)

            // Act
            val result = propertyRegistrationService.getIncompletePropertyFormContextsDeletedThisSession()

            // Assert
            assertEquals(emptyList<Long>(), result)
        }

        @Test
        fun `getFormContextByIdOrNull returns the form context when it exists`() {
            // Arrange
            val formContextId = 1L
            val expectedFormContext = MockLandlordData.createPropertyRegistrationFormContext(id = formContextId)
            whenever(mockFormContextRepository.findById(formContextId))
                .thenReturn(Optional.of(expectedFormContext))

            // Act
            val formContext =
                propertyRegistrationService.getFormContextByIdOrNull(formContextId)

            // Assert
            assertEquals(expectedFormContext, formContext)
        }

        @Test
        fun `getFormContextByIdOrNull returns null when the form context does not exist`() {
            // Arrange
            val formContextId = 1L
            whenever(mockFormContextRepository.findById(formContextId)).thenReturn(Optional.empty())

            // Act, Assert
            assertNull(propertyRegistrationService.getFormContextByIdOrNull(formContextId))
        }
    }
}
