package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

class PropertyDetailsLandlordViewModelBuilderTests {
    @Nested
    inner class LandlordViewTests {
        val currentUserId = "current-user"
        val loggedInLandlord =
            MockLandlordData.createLandlord(
                baseUser = MockLandlordData.createPrsdbUser(currentUserId),
                name = "John Smith",
                email = "john@example.com",
                registrationNumber = RegistrationNumber(RegistrationNumberType.LANDLORD, 1234L),
            )

        @Test
        fun `with single landlord returns one card with LRN and email`() {
            // Arrange, Act
            val cards = PropertyDetailsLandlordViewModelBuilder.buildSummaryCards(setOf(loggedInLandlord), currentUserId, 1L)

            // Assert
            val lrnValue = cards[0].summaryList[0].fieldValue
            assert(lrnValue is RegistrationNumberDataModel)
            assertEquals(1, cards.size)
            val card = cards[0]
            assertEquals(2, card.summaryList.size)
            assertEquals("landlordDetails.personalDetails.lrn", card.summaryList[0].fieldHeading)
            assertEquals("landlordDetails.personalDetails.emailAddress", card.summaryList[1].fieldHeading)
            assertEquals("john@example.com", card.summaryList[1].fieldValue)
            assertEquals(1234L, (card.summaryList[0].fieldValue as RegistrationNumberDataModel).number)
        }

        @Test
        fun `with multiple landlords returns a card per landlord starting with the logged in landlord with a (you) suffix `() {
            // Arrange
            val landlordList =
                setOf(
                    MockLandlordData.createLandlord(
                        baseUser = MockLandlordData.createPrsdbUser("other-user"),
                        name = "Alice Band",
                    ),
                    loggedInLandlord,
                    MockLandlordData.createLandlord(
                        baseUser = MockLandlordData.createPrsdbUser("other-user"),
                        name = "Zack Anderson",
                    ),
                )

            // Act
            val cards = PropertyDetailsLandlordViewModelBuilder.buildSummaryCards(landlordList, currentUserId, 1L)

            // Assert
            assertEquals(3, cards.size)
            assertEquals("propertyDetails.landlordDetails.registeredLandlords.currentUserCardTitle", cards[0].title)
            assertEquals("John Smith", cards[0].cardNumber)
            assertEquals("Alice Band", cards[1].title)
            assertEquals("Zack Anderson", cards[2].title)
        }
    }

    @Nested
    inner class LocalCouncilViewTests {
        @Test
        fun `returns cards sorted alphabetically by landlord name`() {
            val landlords =
                setOf(
                    MockLandlordData.createLandlord(name = "Zoe Adams"),
                    MockLandlordData.createLandlord(name = "Alice Brown"),
                    MockLandlordData.createLandlord(name = "Mike Clark"),
                )

            val cards =
                PropertyDetailsLandlordViewModelBuilder.buildLocalCouncilSummaryCards(
                    landlords,
                    landlordDetailsUrlProvider = { "/local-council/landlord-details/${it.id}" },
                )

            assertEquals(3, cards.size)
            assertEquals("Alice Brown", cards[0].title)
            assertEquals("Mike Clark", cards[1].title)
            assertEquals("Zoe Adams", cards[2].title)
        }

        @Test
        fun `each card contains LRN, email, phone, and contact address rows`() {
            val landlord =
                MockLandlordData.createLandlord(
                    name = "John Smith",
                    email = "john@example.com",
                    phoneNumber = "07712345678",
                )

            val cards =
                PropertyDetailsLandlordViewModelBuilder.buildLocalCouncilSummaryCards(
                    setOf(landlord),
                    landlordDetailsUrlProvider = { "/local-council/landlord-details/${it.id}" },
                )

            val card = cards.single()
            assertEquals(4, card.summaryList.size)
            assertEquals("landlordDetails.personalDetails.lrn", card.summaryList[0].fieldHeading)
            assertEquals("landlordDetails.personalDetails.emailAddress", card.summaryList[1].fieldHeading)
            assertEquals("john@example.com", card.summaryList[1].fieldValue)
            assertEquals("propertyDetails.landlordDetails.contactNumber", card.summaryList[2].fieldHeading)
            assertEquals("07712345678", card.summaryList[2].fieldValue)
            assertEquals("landlordDetails.personalDetails.contactAddress", card.summaryList[3].fieldHeading)
        }

        @Test
        fun `each card has a view landlord record action that opens in new tab`() {
            val landlord = MockLandlordData.createLandlord(name = "John Smith")

            val cards =
                PropertyDetailsLandlordViewModelBuilder.buildLocalCouncilSummaryCards(
                    setOf(landlord),
                    landlordDetailsUrlProvider = { "/local-council/landlord-details/${it.id}" },
                )

            val card = cards.single()
            assertEquals(1, card.actions!!.size)
            assertEquals("propertyDetails.landlordDetails.registeredLandlords.viewLandlordRecord", card.actions!![0].text)
            assertEquals("/local-council/landlord-details/${landlord.id}", card.actions!![0].url)
            assertEquals(true, card.actions!![0].opensInNewTab)
        }
    }
}
