package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
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
        viewModel.forEach { personalDetails -> assert(personalDetails.actions.isEmpty()) }
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

    @Nested
    inner class BuildSummaryCardsTests {
        @Test
        fun `buildSummaryCards with single landlord returns one card with LRN and email`() {
            val landlord =
                MockLandlordData.createLandlord(
                    baseUser = MockLandlordData.createPrsdbUser("user-1"),
                    name = "John Smith",
                    email = "john@example.com",
                )

            val cards = PropertyDetailsLandlordViewModelBuilder.buildSummaryCards(setOf(landlord), "user-1")

            assertEquals(1, cards.size)
            val card = cards[0]
            assertEquals(2, card.summaryList.size)
            assertEquals("landlordDetails.personalDetails.lrn", card.summaryList[0].fieldHeading)
            assertEquals("landlordDetails.personalDetails.emailAddress", card.summaryList[1].fieldHeading)
            assertEquals("john@example.com", card.summaryList[1].fieldValue)
        }

        @Test
        fun `buildSummaryCards with multiple landlords returns card per landlord`() {
            val landlord1 =
                MockLandlordData.createLandlord(
                    baseUser = MockLandlordData.createPrsdbUser("user-1"),
                    name = "John Smith",
                )
            val landlord2 =
                MockLandlordData.createLandlord(
                    baseUser = MockLandlordData.createPrsdbUser("user-2"),
                    name = "Jane Smith",
                )

            val cards = PropertyDetailsLandlordViewModelBuilder.buildSummaryCards(setOf(landlord1, landlord2), "user-1")

            assertEquals(2, cards.size)
        }

        @Test
        fun `buildSummaryCards marks current user with you suffix in title`() {
            val landlord =
                MockLandlordData.createLandlord(
                    baseUser = MockLandlordData.createPrsdbUser("current-user"),
                    name = "John Smith",
                )

            val cards = PropertyDetailsLandlordViewModelBuilder.buildSummaryCards(setOf(landlord), "current-user")

            assertEquals("John Smith (you)", cards[0].title)
        }

        @Test
        fun `buildSummaryCards does not mark other landlords with you suffix`() {
            val landlord =
                MockLandlordData.createLandlord(
                    baseUser = MockLandlordData.createPrsdbUser("other-user"),
                    name = "Jane Smith",
                )

            val cards = PropertyDetailsLandlordViewModelBuilder.buildSummaryCards(setOf(landlord), "current-user")

            assertEquals("Jane Smith", cards[0].title)
        }

        @Test
        fun `buildSummaryCards returns formatted registration number`() {
            val landlord =
                MockLandlordData.createLandlord(
                    baseUser = MockLandlordData.createPrsdbUser("user-1"),
                )

            val cards = PropertyDetailsLandlordViewModelBuilder.buildSummaryCards(setOf(landlord), "user-1")

            val lrnValue = cards[0].summaryList[0].fieldValue
            assert(lrnValue is RegistrationNumberDataModel)
        }

        @Test
        fun `buildSummaryCards returns null actions for each card`() {
            val landlord =
                MockLandlordData.createLandlord(
                    baseUser = MockLandlordData.createPrsdbUser("user-1"),
                )

            val cards = PropertyDetailsLandlordViewModelBuilder.buildSummaryCards(setOf(landlord), "user-1")

            assertEquals(null, cards[0].actions)
        }

        @Test
        fun `buildSummaryCards places current user first in the list`() {
            val otherLandlord =
                MockLandlordData.createLandlord(
                    baseUser = MockLandlordData.createPrsdbUser("other-user"),
                    name = "Jane Smith",
                )
            val currentLandlord =
                MockLandlordData.createLandlord(
                    baseUser = MockLandlordData.createPrsdbUser("current-user"),
                    name = "John Smith",
                )

            val cards =
                PropertyDetailsLandlordViewModelBuilder.buildSummaryCards(
                    setOf(otherLandlord, currentLandlord),
                    "current-user",
                )

            assertEquals("John Smith (you)", cards[0].title)
            assertEquals("Jane Smith", cards[1].title)
        }
    }
}
