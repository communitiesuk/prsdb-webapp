package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.LocalDate

class PropertyDetailsLandlordViewModelBuilderTests {
    @Test
    fun `England or Wales resident landlord details are present and in the correct order`() {
        // Arrange
        val testLandlord = MockLandlordData.createLandlord(nonEnglandOrWalesAddress = null)

        // Act
        val viewModel = PropertyDetailsLandlordViewModelBuilder.fromEntity(testLandlord)

        // Assert
        val landlordDetailsHeaderList = viewModel.map { it.fieldHeading }

        val expectedLandlordDetailsHeaderList =
            listOf(
                "landlordDetails.personalDetails.name",
                "landlordDetails.personalDetails.dateOfBirth",
                "landlordDetails.personalDetails.oneLoginVerified",
                "landlordDetails.personalDetails.emailAddress",
                "propertyDetails.landlordDetails.contactNumber",
                "landlordDetails.personalDetails.contactAddress",
            )

        assertIterableEquals(expectedLandlordDetailsHeaderList, landlordDetailsHeaderList)
    }

    @Test
    fun `Non England or Wales landlord details are present and in the correct order`() {
        // Arrange
        val testLandlord =
            MockLandlordData.createLandlord(
                nonEnglandOrWalesAddress = "1600 Pennsylvania Avenue, Washington DC, United States of America",
                countryOfResidence = "USA",
            )

        // Act
        val viewModel = PropertyDetailsLandlordViewModelBuilder.fromEntity(testLandlord)

        // Assert
        val landlordDetailsHeaderList = viewModel.map { it.fieldHeading }

        val expectedLandlordDetailsHeaderList =
            listOf(
                "landlordDetails.personalDetails.name",
                "landlordDetails.personalDetails.dateOfBirth",
                "landlordDetails.personalDetails.oneLoginVerified",
                "landlordDetails.personalDetails.emailAddress",
                "propertyDetails.landlordDetails.contactNumber",
                "propertyDetails.landlordDetails.addressNonEnglandOrWales",
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
        val viewModel = PropertyDetailsLandlordViewModelBuilder.fromEntity(testLandlord)

        // Assert
        val name =
            viewModel
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
        val viewModel = PropertyDetailsLandlordViewModelBuilder.fromEntity(testLandlord)

        // Assert
        val dateOfBirth =
            viewModel
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
        val viewModel = PropertyDetailsLandlordViewModelBuilder.fromEntity(testLandlord)

        // Assert
        val email =
            viewModel
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
        val viewModel = PropertyDetailsLandlordViewModelBuilder.fromEntity(testLandlord)

        // Assert
        val phoneNumber =
            viewModel
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
        val viewModel = PropertyDetailsLandlordViewModelBuilder.fromEntity(testLandlord)

        // Assert
        val addressString =
            viewModel
                .single { it.fieldHeading == "landlordDetails.personalDetails.contactAddress" }
                .getConvertedFieldValue()
        assertEquals(addressString, oneLineAddress)
    }

    @Test
    fun `Non England or Wales landlord personal shows the residency address`() {
        // Arrange
        val oneLineAddress = "A test address"
        val testLandlord =
            MockLandlordData.createLandlord(nonEnglandOrWalesAddress = oneLineAddress, countryOfResidence = "USA")

        // Act
        val viewModel = PropertyDetailsLandlordViewModelBuilder.fromEntity(testLandlord)

        // Assert
        val addressString =
            viewModel
                .single { it.fieldHeading == "propertyDetails.landlordDetails.addressNonEnglandOrWales" }
                .getConvertedFieldValue()
        assertEquals(addressString, oneLineAddress)
    }

    @Test
    fun `Non England or Wales landlord details shows the correct England or Wales contact address`() {
        // Arrange
        val oneLineAddress = "A test address"
        val testLandlord =
            MockLandlordData.createLandlord(
                address = Address(AddressDataModel(oneLineAddress)),
                nonEnglandOrWalesAddress = "1600 Pennsylvania Avenue, Washington DC, United States of America",
                countryOfResidence = "USA",
            )

        // Act
        val viewModel = PropertyDetailsLandlordViewModelBuilder.fromEntity(testLandlord)

        // Assert
        val addressString =
            viewModel
                .single { it.fieldHeading == "propertyDetails.landlordDetails.contactAddressInEnglandOrWales" }
                .getConvertedFieldValue()
        assertEquals(addressString, oneLineAddress)
    }

    @Test
    fun `LandlordViewModel returns all rows without change links`() {
        // Arrange
        val testLandlord = MockLandlordData.createLandlord()

        // Act
        val viewModel = PropertyDetailsLandlordViewModelBuilder.fromEntity(testLandlord)

        // Assert
        viewModel.forEach { personalDetails -> assertNull(personalDetails.changeUrl) }
    }

    @Test
    fun `Landlord details returns valueUrl for name row`() {
        // Arrange
        val landlordDetailsUrl = "test-url"
        val testLandlord = MockLandlordData.createLandlord()

        // Act
        val viewModel = PropertyDetailsLandlordViewModelBuilder.fromEntity(testLandlord, landlordDetailsUrl = landlordDetailsUrl)

        // Assert
        val returnedLandlordDetailsUrl =
            viewModel
                .single { it.fieldHeading == "landlordDetails.personalDetails.name" }
                .valueUrl

        assertEquals(returnedLandlordDetailsUrl, landlordDetailsUrl)
    }
}
