package uk.gov.communities.prsdb.webapp.models.viewModels

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
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
        val testLandlord = MockLandlordData.createLandlord()

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        assertEquals(viewModel.name, testLandlord.name)
    }

    @Test
    fun `UK based landlord personal details are in the correct order`() {
        // Arrange
        val testLandlord = MockLandlordData.createLandlord(internationalAddress = null)

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val headerList = viewModel.personalDetails.map { it.fieldHeading }

        val expectedHeaderList =
            listOf(
                "landlordDetails.personalDetails.registrationDate",
                "landlordDetails.personalDetails.lrn",
                "landlordDetails.personalDetails.name",
                "landlordDetails.personalDetails.dateOfBirth",
                "landlordDetails.personalDetails.emailAddress",
                "landlordDetails.personalDetails.telephoneNumber",
                "landlordDetails.personalDetails.ukResident",
                "landlordDetails.personalDetails.contactAddress",
            )

        assertIterableEquals(expectedHeaderList, headerList)
    }

    @Test
    fun `Non-UK based landlord personal details are in the correct order`() {
        // Arrange
        val testLandlord =
            MockLandlordData.createLandlord(
                internationalAddress = "1600 Pennsylvania Avenue, Washington DC, United States of America",
            )

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val headerList = viewModel.personalDetails.map { it.fieldHeading }

        val expectedHeaderList =
            listOf(
                "landlordDetails.personalDetails.registrationDate",
                "landlordDetails.personalDetails.lrn",
                "landlordDetails.personalDetails.name",
                "landlordDetails.personalDetails.dateOfBirth",
                "landlordDetails.personalDetails.emailAddress",
                "landlordDetails.personalDetails.telephoneNumber",
                "landlordDetails.personalDetails.ukResident",
                "landlordDetails.personalDetails.country",
                "landlordDetails.personalDetails.nonUkAddress",
                "landlordDetails.personalDetails.ukAddress",
            )

        assertIterableEquals(expectedHeaderList, headerList)
    }

    @ParameterizedTest(name = "on a {0} in {1}")
    @MethodSource("uk.gov.communities.prsdb.webapp.helpers.DateTimeHelperTests#provideInstantsWithLocations")
    fun `Landlord personal details shows the uk date that the user registered`(
        instant: Instant,
        timeZoneID: String,
    ) {
        // Arrange
        val testLandlord =
            MockLandlordData.createLandlord(
                createdAt = OffsetDateTime.ofInstant(instant.toJavaInstant(), ZoneId.of(timeZoneID)).toInstant(),
            )

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
        val registrationNumber =
            RegistrationNumberDataModel.parseTypeOrNull("LGYTKPJRR", RegistrationNumberType.LANDLORD)!!
        val testLandlord =
            MockLandlordData.createLandlord(
                registrationNumber = RegistrationNumber(registrationNumber.type, registrationNumber.number),
            )

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val lrn =
            viewModel.personalDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.lrn" }
                .getConvertedFieldValue()
        assertEquals(lrn, registrationNumber.toString())
    }

    @Test
    fun `Landlord personal details shows the correct name`() {
        // Arrange
        val landlordName = "a test name"
        val testLandlord = MockLandlordData.createLandlord(name = landlordName)

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
        val testLandlord = MockLandlordData.createLandlord(dateOfBirth = landlordDateOfBirth)

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
        val testLandlord = MockLandlordData.createLandlord(email = landlordEmail)

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
        val testLandlord = MockLandlordData.createLandlord(phoneNumber = landlordPhoneNumber)

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
                MockLandlordData.createLandlord(internationalAddress = null)
            } else {
                MockLandlordData.createLandlord(internationalAddress = "1600 Pennsylvania Avenue, Washington DC, United States of America")
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
        val testLandlord = MockLandlordData.createLandlord(address = Address(AddressDataModel(oneLineAddress)))

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
        val testLandlord =
            MockLandlordData.createLandlord(
                internationalAddress = "1600 Pennsylvania Avenue, Washington DC, United States of America",
            )

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
        val testLandlord = MockLandlordData.createLandlord(internationalAddress = oneLineAddress)

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
        val testLandlord =
            MockLandlordData.createLandlord(
                address = Address(AddressDataModel(oneLineAddress)),
                internationalAddress = "1600 Pennsylvania Avenue, Washington DC, United States of America",
            )

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

    @Test
    fun `LandlordViewModel populates change links in rows that should have them`() {
        // Arrange
        val testLandlord = MockLandlordData.createLandlord()

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        assertNull(viewModel.personalDetails[0].changeUrl)
        assertNull(viewModel.personalDetails[1].changeUrl)

        for (i in viewModel.personalDetails.subList(2, viewModel.personalDetails.size)) {
            assertNotNull(i.changeUrl)
        }

        // TODO PRSD-746 change assertion for consentInformation once links have been added
        viewModel.consentInformation.forEach { consentInformation -> assertNull(consentInformation.changeUrl) }
    }

    @Test
    fun `LandlordViewModel returns all rows without change links`() {
        // Arrange
        val testLandlord = MockLandlordData.createLandlord()

        // Act
        val viewModel = LandlordViewModel(testLandlord, withChangeLinks = false)

        // Assert
        viewModel.personalDetails.forEach { personalDetails -> assertNull(personalDetails.changeUrl) }
        viewModel.consentInformation.forEach { consentInformation -> assertNull(consentInformation.changeUrl) }
    }
}
