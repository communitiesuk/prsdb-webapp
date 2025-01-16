package uk.gov.communities.prsdb.webapp.models.viewModels

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

class LandlordViewModelTest {
    @Test
    fun `Name matches the landlord name`() {
        // Arrange
        val testLandlord = MockLandlordData.createMockLandlord()

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        assertEquals(viewModel.name, testLandlord.name)
    }

    @Test
    fun `UK based landlord personal details are in the correct order`() {
        // Arrange
        val testLandlord = MockLandlordData.createMockLandlord()

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val headerList = viewModel.personalDetails.map { it.fieldHeading }

        assertEquals(headerList[0], "landlordDetails.personalDetails.registrationDate")
        assertEquals(headerList[1], "landlordDetails.personalDetails.lrn")
        assertEquals(headerList[2], "landlordDetails.personalDetails.name")
        assertEquals(headerList[3], "landlordDetails.personalDetails.dateOfBirth")
        assertEquals(headerList[4], "landlordDetails.personalDetails.emailAddress")
        assertEquals(headerList[5], "landlordDetails.personalDetails.telephoneNumber")
        assertEquals(headerList[6], "landlordDetails.personalDetails.ukResident")
        assertEquals(headerList[7], "landlordDetails.personalDetails.contactAddress")
    }

    @Test
    fun `Non-UK based landlord personal details are in the correct order`() {
        // Arrange
        val testLandlord = MockLandlordData.createMockNonUkLandlord()

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val headerList = viewModel.personalDetails.map { it.fieldHeading }

        assertEquals(headerList[0], "landlordDetails.personalDetails.registrationDate")
        assertEquals(headerList[1], "landlordDetails.personalDetails.lrn")
        assertEquals(headerList[2], "landlordDetails.personalDetails.name")
        assertEquals(headerList[3], "landlordDetails.personalDetails.dateOfBirth")
        assertEquals(headerList[4], "landlordDetails.personalDetails.emailAddress")
        assertEquals(headerList[5], "landlordDetails.personalDetails.telephoneNumber")
        assertEquals(headerList[6], "landlordDetails.personalDetails.ukResident")
        assertEquals(headerList[7], "landlordDetails.personalDetails.country")
        assertEquals(headerList[8], "landlordDetails.personalDetails.nonUkAddress")
        assertEquals(headerList[9], "landlordDetails.personalDetails.ukAddress")
    }

    @ParameterizedTest(name = "on a {0} in {1}")
    @MethodSource("uk.gov.communities.prsdb.webapp.helpers.DateTimeHelperTests#provideInstantsWithLocations")
    fun `Landlord personal details shows the uk date that the user registered`(
        instant: Instant,
        timeZoneID: String,
    ) {
        // Arrange
        val testLandlord = MockLandlordData.createMockNonUkLandlord()
        whenever(testLandlord.createdDate).thenReturn(OffsetDateTime.ofInstant(instant.toJavaInstant(), ZoneId.of(timeZoneID)))

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val registrationDate =
            viewModel.personalDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.registrationDate" }
                .getConvertedFieldValue()

        assertEquals(registrationDate, instant.toLocalDateTime(TimeZone.of("Europe/London")).date.toJavaLocalDate())
    }

    @Test
    fun `Landlord personal details shows the correct lrn`() {
        // Arrange
        val registrationNumber = RegistrationNumberDataModel.parse("LGYTKPJRR")
        val testLandlord = MockLandlordData.createMockNonUkLandlord()
        whenever(testLandlord.registrationNumber).thenReturn(RegistrationNumber(registrationNumber.type, registrationNumber.number))

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val lrn = viewModel.personalDetails.single { it.fieldHeading == "landlordDetails.personalDetails.lrn" }.getConvertedFieldValue()
        assertEquals(lrn, registrationNumber.toString())
    }

    @Test
    fun `Landlord personal details shows the correct name`() {
        // Arrange
        val landlordName = "a test name"
        val testLandlord = MockLandlordData.createMockNonUkLandlord()
        whenever(testLandlord.name).thenReturn(landlordName)

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val name =
            viewModel.personalDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.name" }
                .getConvertedFieldValue()

        assertEquals(name, landlordName)
    }

    @Test
    fun `Landlord personal details shows the correct date of birth`() {
        // Arrange
        val landlordDateOfBirth = LocalDate.ofEpochDay(10000)
        val testLandlord = MockLandlordData.createMockNonUkLandlord()
        whenever(testLandlord.dateOfBirth).thenReturn(landlordDateOfBirth)

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val dateOfBirth =
            viewModel.personalDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.dateOfBirth" }
                .getConvertedFieldValue()

        assertEquals(dateOfBirth, landlordDateOfBirth)
    }

    @Test
    fun `Landlord personal details shows the correct email address`() {
        // Arrange
        val landlordEmail = "an email address"
        val testLandlord = MockLandlordData.createMockNonUkLandlord()
        whenever(testLandlord.email).thenReturn(landlordEmail)

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val email =
            viewModel.personalDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.emailAddress" }
                .getConvertedFieldValue()
        assertEquals(email, landlordEmail)
    }

    @Test
    fun `Landlord personal details shows the correct telephone number`() {
        // Arrange
        val landlordPhoneNumber = "a phone number"
        val testLandlord = MockLandlordData.createMockNonUkLandlord()
        whenever(testLandlord.phoneNumber).thenReturn(landlordPhoneNumber)

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val phoneNumber =
            viewModel.personalDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.telephoneNumber" }
                .getConvertedFieldValue()
        assertEquals(phoneNumber, landlordPhoneNumber)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Landlord personal details shows the correct residency`(isUkResident: Boolean) {
        // Arrange
        val testLandlord =
            if (isUkResident) {
                MockLandlordData.createMockLandlord()
            } else {
                MockLandlordData.createMockNonUkLandlord()
            }

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val displayedResidency =
            viewModel.personalDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.ukResident" }
                .fieldValue
        assertEquals(displayedResidency, isUkResident)
    }

    @Test
    fun `UK landlord personal details shows the correct contact address`() {
        // Arrange
        val oneLineAddress = "A test address"
        val testLandlord = MockLandlordData.createMockLandlord()
        whenever(testLandlord.address).thenReturn(Address(AddressDataModel(oneLineAddress)))

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val addressString =
            viewModel.personalDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.contactAddress" }
                .getConvertedFieldValue()
        assertEquals(addressString, oneLineAddress)
    }

    @Test
    @Disabled
    fun `Non UK landlord personal details shows the correct country of residency`() {
        // Arrange
        val testLandlord = MockLandlordData.createMockNonUkLandlord()

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val country =
            viewModel.personalDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.country" }
                .getConvertedFieldValue()

        TODO("PRSD-742")
    }

    @Test
    fun `Non UK landlord personal details shows the residency address`() {
        // Arrange
        val oneLineAddress = "A test address"
        val testLandlord = MockLandlordData.createMockNonUkLandlord()
        whenever(testLandlord.internationalAddress).thenReturn(oneLineAddress)

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val addressString =
            viewModel.personalDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.nonUkAddress" }
                .getConvertedFieldValue()
        assertEquals(addressString, oneLineAddress)
    }

    @Test
    fun `Non UK landlord personal details shows the correct UK contact address`() {
        // Arrange
        val oneLineAddress = "A test address"
        val testLandlord = MockLandlordData.createMockNonUkLandlord()
        whenever(testLandlord.address).thenReturn(Address(AddressDataModel(oneLineAddress)))

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val addressString =
            viewModel.personalDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.ukAddress" }
                .getConvertedFieldValue()
        assertEquals(addressString, oneLineAddress)
    }

    @Disabled
    @Test
    fun getConsentInformation() {
        TODO("PRSD-746")
    }
}
