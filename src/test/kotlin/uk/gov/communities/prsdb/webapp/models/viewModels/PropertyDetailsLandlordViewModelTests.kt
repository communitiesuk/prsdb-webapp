package uk.gov.communities.prsdb.webapp.models.viewModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import java.time.LocalDate

class PropertyDetailsLandlordViewModelTests {
    @Test
    fun `England or Wales resident landlord details are present and in the correct order`() {
        // Arrange
        val testLandlord = MockLandlordData.createLandlord(nonEnglandOrWalesAddress = null)

        // Act
        val viewModel = PropertyDetailsLandlordViewModel(testLandlord)

        // Assert
        val landlordDetailsHeaderList = viewModel.landlordsDetails.map { it.fieldHeading }

        val expectedLandlordDetailsHeaderList =
            listOf(
                "landlordDetails.personalDetails.name",
                "landlordDetails.personalDetails.dateOfBirth",
                "landlordDetails.personalDetails.emailAddress",
                "propertyDetails.landlordDetails.contactNumber",
                "landlordDetails.personalDetails.contactAddress",
            )

        assertIterableEquals(expectedLandlordDetailsHeaderList, landlordDetailsHeaderList)
    }

    @Test
    fun `Non-UK based landlord details are present and in the correct order`() {
        // Arrange
        val testLandlord =
            MockLandlordData.createLandlord(
                nonEnglandOrWalesAddress = "1600 Pennsylvania Avenue, Washington DC, United States of America",
                countryOfResidence = "USA",
            )

        // Act
        val viewModel = PropertyDetailsLandlordViewModel(testLandlord)

        // Assert
        val landlordDetailsHeaderList = viewModel.landlordsDetails.map { it.fieldHeading }

        val expectedLandlordDetailsHeaderList =
            listOf(
                "landlordDetails.personalDetails.name",
                "landlordDetails.personalDetails.dateOfBirth",
                "landlordDetails.personalDetails.emailAddress",
                "propertyDetails.landlordDetails.contactNumber",
                "propertyDetails.landlordDetails.addressOutsideEnglandOrWales",
                "propertyDetails.landlordDetails.contactAddressInEnglandOrWales",
            )

        assertIterableEquals(expectedLandlordDetailsHeaderList, landlordDetailsHeaderList)
    }

    @Test
    fun `Landlord details shows the correct name`() {
        // Arrange
        val landlordName = "a test name"
        val testLandlord = MockLandlordData.createLandlord(name = landlordName)

        // Act
        val viewModel = PropertyDetailsLandlordViewModel(testLandlord)

        // Assert
        val name =
            viewModel.landlordsDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.name" }
                .getConvertedFieldValue()

        assertEquals(name, landlordName)
    }

    @Test
    fun `Landlord details shows the correct date of birth`() {
        // Arrange
        val landlordDateOfBirth = LocalDate.ofEpochDay(10000)
        val testLandlord = MockLandlordData.createLandlord(dateOfBirth = landlordDateOfBirth)

        // Act
        val viewModel = PropertyDetailsLandlordViewModel(testLandlord)

        // Assert
        val dateOfBirth =
            viewModel.landlordsDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.dateOfBirth" }
                .getConvertedFieldValue()

        assertEquals(dateOfBirth, landlordDateOfBirth)
    }

    @Test
    fun `Landlord details shows the correct email address`() {
        // Arrange
        val landlordEmail = "an email address"
        val testLandlord = MockLandlordData.createLandlord(email = landlordEmail)

        // Act
        val viewModel = PropertyDetailsLandlordViewModel(testLandlord)

        // Assert
        val email =
            viewModel.landlordsDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.emailAddress" }
                .getConvertedFieldValue()
        assertEquals(email, landlordEmail)
    }

    @Test
    fun `Landlord details shows the correct telephone number`() {
        // Arrange
        val landlordPhoneNumber = "a phone number"
        val testLandlord = MockLandlordData.createLandlord(phoneNumber = landlordPhoneNumber)

        // Act
        val viewModel = PropertyDetailsLandlordViewModel(testLandlord)

        // Assert
        val phoneNumber =
            viewModel.landlordsDetails
                .single { it.fieldHeading == "propertyDetails.landlordDetails.contactNumber" }
                .getConvertedFieldValue()
        assertEquals(phoneNumber, landlordPhoneNumber)
    }

    @Test
    fun `England or Wales landlord details shows the correct contact address`() {
        // Arrange
        val oneLineAddress = "A test address"
        val testLandlord = MockLandlordData.createLandlord(address = Address(AddressDataModel(oneLineAddress)))

        // Act
        val viewModel = PropertyDetailsLandlordViewModel(testLandlord)

        // Assert
        val addressString =
            viewModel.landlordsDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.contactAddress" }
                .getConvertedFieldValue()
        assertEquals(addressString, oneLineAddress)
    }

    @Test
    fun `Non UK landlord personal shows the residency address`() {
        // Arrange
        val oneLineAddress = "A test address"
        val testLandlord = MockLandlordData.createLandlord(nonEnglandOrWalesAddress = oneLineAddress, countryOfResidence = "USA")

        // Act
        val viewModel = PropertyDetailsLandlordViewModel(testLandlord)

        // Assert
        val addressString =
            viewModel.landlordsDetails
                .single { it.fieldHeading == "propertyDetails.landlordDetails.addressOutsideEnglandOrWales" }
                .getConvertedFieldValue()
        assertEquals(addressString, oneLineAddress)
    }

    @Test
    fun `Non UK landlord details shows the correct UK contact address`() {
        // Arrange
        val oneLineAddress = "A test address"
        val testLandlord =
            MockLandlordData.createLandlord(
                address = Address(AddressDataModel(oneLineAddress)),
                nonEnglandOrWalesAddress = "1600 Pennsylvania Avenue, Washington DC, United States of America",
                countryOfResidence = "USA",
            )

        // Act
        val viewModel = PropertyDetailsLandlordViewModel(testLandlord)

        // Assert
        val addressString =
            viewModel.landlordsDetails
                .single { it.fieldHeading == "propertyDetails.landlordDetails.contactAddressInEnglandOrWales" }
                .getConvertedFieldValue()
        assertEquals(addressString, oneLineAddress)
    }

    @Test
    fun `LandlordViewModel populates change links in rows that should have them`() {
        // Arrange
        val testLandlord = MockLandlordData.createLandlord()

        // Act
        val viewModel = PropertyDetailsLandlordViewModel(testLandlord)

        // Assert
        for (i in viewModel.landlordsDetails) {
            assertNotNull(i.changeUrl)
        }
    }

    @Test
    fun `LandlordViewModel returns all rows without change links`() {
        // Arrange
        val testLandlord = MockLandlordData.createLandlord()

        // Act
        val viewModel = PropertyDetailsLandlordViewModel(testLandlord, withChangeLinks = false)

        // Assert
        viewModel.landlordsDetails.forEach { personalDetails -> assertNull(personalDetails.changeUrl) }
    }

    @Test
    fun `Landlord details returns valueUrl for name row`() {
        // Arrange
        val landlordDetailsUrl = "test-url"
        val testLandlord = MockLandlordData.createLandlord()

        // Act
        val viewModel = PropertyDetailsLandlordViewModel(testLandlord, landlordDetailsUrl = landlordDetailsUrl)

        // Assert
        val returnedLandlordDetailsUrl =
            viewModel.landlordsDetails
                .single { it.fieldHeading == "landlordDetails.personalDetails.name" }
                .valueUrl

        assertEquals(returnedLandlordDetailsUrl, landlordDetailsUrl)
    }
}
