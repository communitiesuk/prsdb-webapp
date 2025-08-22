package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

class LandlordViewModelTests {
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
    fun `England or Wales resident landlord personal details are in the correct order`() {
        // Arrange
        val testLandlord = MockLandlordData.createLandlord(nonEnglandOrWalesAddress = null)

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
                "landlordDetails.personalDetails.oneLoginVerified",
                "landlordDetails.personalDetails.emailAddress",
                "landlordDetails.personalDetails.telephoneNumber",
                "landlordDetails.personalDetails.contactAddress",
            )

        assertIterableEquals(expectedHeaderList, headerList)
    }

    @Test
    fun `Non England or Wales landlord personal details are in the correct order`() {
        // Arrange
        val testLandlord =
            MockLandlordData.createLandlord(
                nonEnglandOrWalesAddress = "1600 Pennsylvania Avenue, Washington DC, United States of America",
                countryOfResidence = "USA",
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
                "landlordDetails.personalDetails.oneLoginVerified",
                "landlordDetails.personalDetails.emailAddress",
                "landlordDetails.personalDetails.telephoneNumber",
                "landlordDetails.personalDetails.country",
                "landlordDetails.personalDetails.nonEnglandOrWalesAddress",
                "landlordDetails.personalDetails.englandOrWalesAddress",
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
                createdDate = OffsetDateTime.ofInstant(instant.toJavaInstant(), ZoneId.of(timeZoneID)).toInstant(),
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

    @Test
    fun `England or Wales  landlord personal details shows the correct contact address`() {
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
    fun `Non England or Wales resident landlord personal details shows the correct country of residency`() {
        // Arrange
        val countryOfResidence = "Barbados"
        val testLandlord =
            MockLandlordData.createLandlord(
                countryOfResidence = countryOfResidence,
            )

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val country =
            viewModel.personalDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.country" }
                .getConvertedFieldValue()

        assertEquals(country, countryOfResidence)
    }

    @Test
    fun `Non England or Wales landlord personal details shows the residency address`() {
        // Arrange
        val oneLineAddress = "A test address"
        val testLandlord =
            MockLandlordData.createLandlord(nonEnglandOrWalesAddress = oneLineAddress, countryOfResidence = "USA")

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val addressString =
            viewModel.personalDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.nonEnglandOrWalesAddress" }
                .getConvertedFieldValue()
        assertEquals(addressString, oneLineAddress)
    }

    @Test
    fun `Non England or Wales landlord personal details shows the correct England or Wales contact address`() {
        // Arrange
        val oneLineAddress = "A test address"
        val testLandlord =
            MockLandlordData.createLandlord(
                address = Address(AddressDataModel(oneLineAddress)),
                nonEnglandOrWalesAddress = "1600 Pennsylvania Avenue, Washington DC, United States of America",
                countryOfResidence = "USA",
            )

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        val addressString =
            viewModel.personalDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.englandOrWalesAddress" }
                .getConvertedFieldValue()
        assertEquals(addressString, oneLineAddress)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `LandlordViewModel populates change links in rows when landlord`(isVerified: Boolean) {
        // Arrange
        val testLandlord = MockLandlordData.createLandlord(isVerified = isVerified)
        val changeableByAllLandlordsPersonalDetailKeys =
            listOf(
                "landlordDetails.personalDetails.emailAddress",
                "landlordDetails.personalDetails.telephoneNumber",
                // TODO PRSD-355 (address update): uncomment
                // "landlordDetails.personalDetails.contactAddress",
            )
        val changeableByUnverifiedLandlordsPersonalDetailKeys =
            listOf(
                "landlordDetails.personalDetails.name",
                "landlordDetails.personalDetails.dateOfBirth",
            )

        // Act
        val viewModel = LandlordViewModel(testLandlord)

        // Assert
        for (i in viewModel.personalDetails.filter { detail -> detail.fieldHeading in changeableByAllLandlordsPersonalDetailKeys }) {
            assertNotNull(i.action)
        }

        if (isVerified) {
            for (i in viewModel.personalDetails.filter { detail ->
                detail.fieldHeading !in changeableByAllLandlordsPersonalDetailKeys
            }) {
                assertNull(i.action)
            }
        } else {
            for (i in viewModel.personalDetails.filter { detail ->
                detail.fieldHeading in changeableByUnverifiedLandlordsPersonalDetailKeys
            }) {
                assertNotNull(i.action)
            }
            for (i in viewModel.personalDetails.filter { detail ->
                detail.fieldHeading !in changeableByAllLandlordsPersonalDetailKeys + changeableByUnverifiedLandlordsPersonalDetailKeys
            }) {
                assertNull(i.action)
            }
        }
    }

    @Test
    fun `LandlordViewModel returns all rows without change links`() {
        // Arrange
        val testLandlord = MockLandlordData.createLandlord()

        // Act
        val viewModel = LandlordViewModel(testLandlord, withChangeLinks = false)

        // Assert
        viewModel.personalDetails.forEach { personalDetails -> assertNull(personalDetails.action) }
    }
}
