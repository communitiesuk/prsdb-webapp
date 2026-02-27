package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

@ExtendWith(MockitoExtension::class)
class PropertyRegistrationServiceTests {
    @Mock
    private lateinit var mockPropertyOwnershipRepository: PropertyOwnershipRepository

    @Mock
    private lateinit var mockLandlordRepository: LandlordRepository

    @Mock
    private lateinit var mockAddressService: AddressService

    @Mock
    private lateinit var mockLicenseService: LicenseService

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var mockConfirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationEmail>

    @Mock
    private lateinit var mockPropertyRegistrationConfirmationService: PropertyRegistrationConfirmationService

    @Mock
    private lateinit var mockJointLandlordInvitationService: JointLandlordInvitationService

    @InjectMocks
    private lateinit var propertyRegistrationService: PropertyRegistrationService

    @Test
    fun `registerProperty throws an error if the given address is registered`() {
        val registeredAddress = AddressDataModel(singleLineAddress = "1 Example Road", uprn = 0L)

        whenever(
            mockPropertyOwnershipRepository.existsByIsActiveTrueAndAddress_Uprn(registeredAddress.uprn!!),
        ).thenReturn(true)

        val errorThrown =
            assertThrows<EntityExistsException> {
                propertyRegistrationService.registerProperty(
                    registeredAddress,
                    PropertyType.DETACHED_HOUSE,
                    LicensingType.NO_LICENSING,
                    "license number",
                    OwnershipType.FREEHOLD,
                    1,
                    1,
                    "baseUserId",
                    1,
                    null,
                    null,
                    null,
                    RentFrequency.MONTHLY,
                    null,
                    123.toBigDecimal(),
                    null,
                )
            }

        assertEquals("Address already registered", errorThrown.message)
    }

    @Test
    fun `registerProperty throws an error if the logged in user is not a landlord`() {
        val nonLandlordUserId = "baseUserId"
        val address = AddressDataModel("1 Example Road")

        whenever(mockLandlordRepository.findByBaseUser_Id(nonLandlordUserId)).thenReturn(null)

        val errorThrown =
            assertThrows<EntityNotFoundException> {
                propertyRegistrationService.registerProperty(
                    address,
                    PropertyType.DETACHED_HOUSE,
                    LicensingType.NO_LICENSING,
                    "license number",
                    OwnershipType.FREEHOLD,
                    1,
                    1,
                    nonLandlordUserId,
                    1,
                    null,
                    null,
                    null,
                    RentFrequency.MONTHLY,
                    null,
                    123.toBigDecimal(),
                    null,
                )
            }

        assertEquals("User not registered as a landlord", errorThrown.message)
    }

    @Test
    fun `registerProperty registers the property if all fields are populated`() {
        // Arrange
        val ownershipType = OwnershipType.FREEHOLD
        val numberOfHouseholds = 1
        val numberOfPeople = 2
        val landlord = MockLandlordData.createLandlord()
        val propertyType = PropertyType.OTHER
        val customPropertyType = "End terrace"
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val address = Address(addressDataModel)
        val licenceType = LicensingType.SELECTIVE_LICENCE
        val licenceNumber = "L1234"
        val licence = License(licenceType, licenceNumber)
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)
        val numberOfBedrooms = 1
        val billsIncludedList = "Electricity, Water"
        val customBillsIncluded = "Internet"
        val furnishedStatus = FurnishedStatus.FURNISHED
        val rentFrequency = RentFrequency.OTHER
        val customRentFrequency = "Fortnightly"
        val rentAmount = 123.toBigDecimal()

        val expectedPropertyOwnership =
            MockLandlordData.createPropertyOwnership(
                ownershipType = ownershipType,
                currentNumHouseholds = numberOfHouseholds,
                currentNumTenants = numberOfPeople,
                primaryLandlord = landlord,
                propertyBuildType = propertyType,
                customPropertyType = customPropertyType,
                address = address,
                license = licence,
                registrationNumber = registrationNumber,
                numberOfBedrooms = numberOfBedrooms,
                billsIncludedList = billsIncludedList,
                customBillsIncluded = customBillsIncluded,
                furnishedStatus = furnishedStatus,
                rentFrequency = rentFrequency,
                customRentFrequency = customRentFrequency,
                rentAmount = rentAmount,
            )

        whenever(mockAddressService.findOrCreateAddress(addressDataModel)).thenReturn(address)
        whenever(mockLandlordRepository.findByBaseUser_Id(landlord.baseUser.id)).thenReturn(landlord)
        whenever(mockLicenseService.createLicense(licenceType, licenceNumber)).thenReturn(licence)
        whenever(
            mockPropertyOwnershipService.createPropertyOwnership(
                ownershipType = ownershipType,
                numberOfHouseholds = numberOfHouseholds,
                numberOfPeople = numberOfPeople,
                primaryLandlord = landlord,
                propertyBuildType = propertyType,
                customPropertyType = customPropertyType,
                address = address,
                license = licence,
                numBedrooms = numberOfBedrooms,
                billsIncludedList = billsIncludedList,
                customBillsIncluded = customBillsIncluded,
                furnishedStatus = furnishedStatus,
                rentFrequency = rentFrequency,
                customRentFrequency = customRentFrequency,
                rentAmount = rentAmount,
            ),
        ).thenReturn(expectedPropertyOwnership)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("https:gov.uk"))

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
                numberOfBedrooms,
                billsIncludedList,
                customBillsIncluded,
                furnishedStatus,
                rentFrequency,
                customRentFrequency,
                rentAmount,
                customPropertyType,
            )

        // Assert
        assertEquals(expectedPropertyOwnership.registrationNumber, propertyRegistrationNumber)
        verify(mockPropertyOwnershipService).createPropertyOwnership(
            ownershipType = ownershipType,
            numberOfHouseholds = numberOfHouseholds,
            numberOfPeople = numberOfPeople,
            primaryLandlord = landlord,
            propertyBuildType = propertyType,
            customPropertyType = customPropertyType,
            address = address,
            license = licence,
            numBedrooms = numberOfBedrooms,
            billsIncludedList = billsIncludedList,
            customBillsIncluded = customBillsIncluded,
            furnishedStatus = furnishedStatus,
            rentFrequency = rentFrequency,
            customRentFrequency = customRentFrequency,
            rentAmount = rentAmount,
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

        whenever(mockAddressService.findOrCreateAddress(any())).thenReturn(expectedPropertyOwnership.address)
        whenever(mockLandlordRepository.findByBaseUser_Id(any())).thenReturn(landlord)
        whenever(mockLicenseService.createLicense(any(), any())).thenReturn(expectedPropertyOwnership.license)
        whenever(
            mockPropertyOwnershipService.createPropertyOwnership(
                ownershipType = any(),
                numberOfHouseholds = any(),
                numberOfPeople = any(),
                primaryLandlord = any(),
                propertyBuildType = any(),
                address = any(),
                license = anyOrNull(),
                isActive = any(),
                numBedrooms = anyOrNull(),
                billsIncludedList = anyOrNull(),
                customBillsIncluded = anyOrNull(),
                furnishedStatus = anyOrNull(),
                rentFrequency = anyOrNull(),
                customRentFrequency = anyOrNull(),
                rentAmount = anyOrNull(),
                customPropertyType = anyOrNull(),
            ),
        ).thenReturn(expectedPropertyOwnership)

        val dashboardUri = URI("https:gov.uk")
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)

        // Act
        propertyRegistrationService.registerProperty(
            AddressDataModel.fromAddress(expectedPropertyOwnership.address),
            PropertyType.DETACHED_HOUSE,
            LicensingType.SELECTIVE_LICENCE,
            "Licence",
            OwnershipType.FREEHOLD,
            2,
            3,
            "USER_ID",
            1,
            null,
            null,
            null,
            RentFrequency.MONTHLY,
            null,
            123.toBigDecimal(),
            null,
        )

        // Assert
        verify(mockConfirmationEmailSender).sendEmail(
            eq(landlord.email),
            argThat<PropertyRegistrationConfirmationEmail> { email ->
                email.prn == RegistrationNumberDataModel.fromRegistrationNumber(registrationNumber).toString() &&
                    email.singleLineAddress == expectedPropertyOwnership.address.singleLineAddress &&
                    email.prsdUrl == dashboardUri.toString() &&
                    email.isOccupied == (expectedPropertyOwnership.currentNumTenants > 0)
            },
        )

        verify(mockPropertyRegistrationConfirmationService).setLastPrnRegisteredThisSession(eq(registrationNumber.number))
    }

    @Test
    fun `registerProperty registers the property if there is no license`() {
        // Arrange
        val ownershipType = OwnershipType.FREEHOLD
        val numberOfHouseholds = 1
        val numberOfPeople = 2
        val landlord = MockLandlordData.createLandlord()
        val propertyType = PropertyType.OTHER
        val customPropertyType = "End terrace"
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val address = Address(addressDataModel)
        val licenceType = LicensingType.NO_LICENSING
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)
        val numberOfBedrooms = 1
        val billsIncludedList = "Electricity, Water"
        val customBillsIncluded = "Internet"
        val furnishedStatus = FurnishedStatus.FURNISHED
        val rentFrequency = RentFrequency.OTHER
        val customRentFrequency = "Fortnightly"
        val rentAmount = 123.toBigDecimal()

        val expectedPropertyOwnership =
            MockLandlordData.createPropertyOwnership(
                ownershipType = ownershipType,
                currentNumHouseholds = numberOfHouseholds,
                currentNumTenants = numberOfPeople,
                primaryLandlord = landlord,
                propertyBuildType = propertyType,
                address = address,
                license = null,
                registrationNumber = registrationNumber,
                numberOfBedrooms = numberOfBedrooms,
                billsIncludedList = billsIncludedList,
                customBillsIncluded = customBillsIncluded,
                furnishedStatus = furnishedStatus,
                rentFrequency = rentFrequency,
                customRentFrequency = customRentFrequency,
                rentAmount = rentAmount,
            )

        whenever(mockAddressService.findOrCreateAddress(addressDataModel)).thenReturn(address)
        whenever(mockLandlordRepository.findByBaseUser_Id(landlord.baseUser.id)).thenReturn(landlord)
        whenever(
            mockPropertyOwnershipService.createPropertyOwnership(
                ownershipType = ownershipType,
                numberOfHouseholds = numberOfHouseholds,
                numberOfPeople = numberOfPeople,
                primaryLandlord = landlord,
                propertyBuildType = propertyType,
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
            ),
        ).thenReturn(expectedPropertyOwnership)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("https:gov.uk"))

        val propertyRegistrationNumber =
            propertyRegistrationService.registerProperty(
                addressDataModel,
                propertyType,
                licenceType,
                licenceNumber = "",
                ownershipType,
                numberOfHouseholds,
                numberOfPeople,
                landlord.baseUser.id,
                numberOfBedrooms,
                billsIncludedList,
                customBillsIncluded,
                furnishedStatus,
                rentFrequency,
                customRentFrequency,
                rentAmount,
                customPropertyType,
            )

        assertEquals(expectedPropertyOwnership.registrationNumber, propertyRegistrationNumber)
    }

    @Test
    fun `registerProperty sends joint landlord invitation emails when joint landlord emails are provided`() {
        // Arrange
        val jointLandlordEmails = listOf("landlord1@example.com", "landlord2@example.com")
        val ownershipType = OwnershipType.FREEHOLD
        val numberOfHouseholds = 1
        val numberOfPeople = 2
        val landlord = MockLandlordData.createLandlord()
        val propertyType = PropertyType.DETACHED_HOUSE
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val address = Address(addressDataModel)
        val licenceType = LicensingType.SELECTIVE_LICENCE
        val licenceNumber = "Licence123"
        val license = License(licenceType, licenceNumber)
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)

        val expectedPropertyOwnership =
            MockLandlordData.createPropertyOwnership(
                ownershipType = ownershipType,
                currentNumHouseholds = numberOfHouseholds,
                currentNumTenants = numberOfPeople,
                primaryLandlord = landlord,
                propertyBuildType = propertyType,
                address = address,
                license = license,
                registrationNumber = registrationNumber,
            )

        whenever(mockAddressService.findOrCreateAddress(addressDataModel)).thenReturn(address)
        whenever(mockLandlordRepository.findByBaseUser_Id(landlord.baseUser.id)).thenReturn(landlord)
        whenever(mockLicenseService.createLicense(licenceType, licenceNumber)).thenReturn(license)
        whenever(
            mockPropertyOwnershipService.createPropertyOwnership(
                ownershipType = ownershipType,
                numberOfHouseholds = numberOfHouseholds,
                numberOfPeople = numberOfPeople,
                primaryLandlord = landlord,
                propertyBuildType = propertyType,
                customPropertyType = null,
                address = address,
                license = license,
                isActive = true,
                numBedrooms = null,
                billsIncludedList = null,
                customBillsIncluded = null,
                furnishedStatus = null,
                rentFrequency = RentFrequency.MONTHLY,
                customRentFrequency = null,
                rentAmount = 123.toBigDecimal(),
            ),
        ).thenReturn(expectedPropertyOwnership)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("https:gov.uk"))

        // Act
        propertyRegistrationService.registerProperty(
            addressDataModel,
            propertyType,
            licenceType,
            licenceNumber,
            ownershipType,
            numberOfHouseholds,
            numberOfPeople,
            landlord.baseUser.id,
            null,
            null,
            null,
            null,
            RentFrequency.MONTHLY,
            null,
            123.toBigDecimal(),
            null,
            jointLandlordEmails,
        )

        // Assert
        verify(mockJointLandlordInvitationService).sendInvitationEmails(
            eq(jointLandlordEmails),
            eq(expectedPropertyOwnership),
        )
    }

    @Test
    fun `registerProperty does not send joint landlord invitation emails when no joint landlord emails provided`() {
        // Arrange
        val ownershipType = OwnershipType.FREEHOLD
        val numberOfHouseholds = 1
        val numberOfPeople = 2
        val landlord = MockLandlordData.createLandlord()
        val propertyType = PropertyType.DETACHED_HOUSE
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val address = Address(addressDataModel)
        val licenceType = LicensingType.NO_LICENSING
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)

        val expectedPropertyOwnership =
            MockLandlordData.createPropertyOwnership(
                ownershipType = ownershipType,
                currentNumHouseholds = numberOfHouseholds,
                currentNumTenants = numberOfPeople,
                primaryLandlord = landlord,
                propertyBuildType = propertyType,
                address = address,
                license = null,
                registrationNumber = registrationNumber,
            )

        whenever(mockAddressService.findOrCreateAddress(addressDataModel)).thenReturn(address)
        whenever(mockLandlordRepository.findByBaseUser_Id(landlord.baseUser.id)).thenReturn(landlord)
        whenever(
            mockPropertyOwnershipService.createPropertyOwnership(
                ownershipType = ownershipType,
                numberOfHouseholds = numberOfHouseholds,
                numberOfPeople = numberOfPeople,
                primaryLandlord = landlord,
                propertyBuildType = propertyType,
                customPropertyType = null,
                address = address,
                license = null,
                isActive = true,
                numBedrooms = null,
                billsIncludedList = null,
                customBillsIncluded = null,
                furnishedStatus = null,
                rentFrequency = RentFrequency.MONTHLY,
                customRentFrequency = null,
                rentAmount = 123.toBigDecimal(),
            ),
        ).thenReturn(expectedPropertyOwnership)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("https:gov.uk"))

        // Act
        propertyRegistrationService.registerProperty(
            addressDataModel,
            propertyType,
            licenceType,
            "",
            ownershipType,
            numberOfHouseholds,
            numberOfPeople,
            landlord.baseUser.id,
            null,
            null,
            null,
            null,
            RentFrequency.MONTHLY,
            null,
            123.toBigDecimal(),
            null,
            null,
        )

        // Assert
        org.mockito.Mockito.verifyNoInteractions(mockJointLandlordInvitationService)
    }

    @Test
    fun `registerProperty does not send joint landlord invitation emails when empty list provided`() {
        // Arrange
        val jointLandlordEmails = emptyList<String>()
        val ownershipType = OwnershipType.FREEHOLD
        val numberOfHouseholds = 1
        val numberOfPeople = 2
        val landlord = MockLandlordData.createLandlord()
        val propertyType = PropertyType.DETACHED_HOUSE
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")
        val address = Address(addressDataModel)
        val licenceType = LicensingType.NO_LICENSING
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)

        val expectedPropertyOwnership =
            MockLandlordData.createPropertyOwnership(
                ownershipType = ownershipType,
                currentNumHouseholds = numberOfHouseholds,
                currentNumTenants = numberOfPeople,
                primaryLandlord = landlord,
                propertyBuildType = propertyType,
                address = address,
                license = null,
                registrationNumber = registrationNumber,
            )

        whenever(mockAddressService.findOrCreateAddress(addressDataModel)).thenReturn(address)
        whenever(mockLandlordRepository.findByBaseUser_Id(landlord.baseUser.id)).thenReturn(landlord)
        whenever(
            mockPropertyOwnershipService.createPropertyOwnership(
                ownershipType = ownershipType,
                numberOfHouseholds = numberOfHouseholds,
                numberOfPeople = numberOfPeople,
                primaryLandlord = landlord,
                propertyBuildType = propertyType,
                customPropertyType = null,
                address = address,
                license = null,
                isActive = true,
                numBedrooms = null,
                billsIncludedList = null,
                customBillsIncluded = null,
                furnishedStatus = null,
                rentFrequency = RentFrequency.MONTHLY,
                customRentFrequency = null,
                rentAmount = 123.toBigDecimal(),
            ),
        ).thenReturn(expectedPropertyOwnership)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("https:gov.uk"))

        // Act
        propertyRegistrationService.registerProperty(
            addressDataModel,
            propertyType,
            licenceType,
            "",
            ownershipType,
            numberOfHouseholds,
            numberOfPeople,
            landlord.baseUser.id,
            null,
            null,
            null,
            null,
            RentFrequency.MONTHLY,
            null,
            123.toBigDecimal(),
            null,
            jointLandlordEmails,
        )

        // Assert
        org.mockito.Mockito.verifyNoInteractions(mockJointLandlordInvitationService)
    }
}
