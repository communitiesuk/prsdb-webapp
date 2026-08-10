package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

class PropertyDetailsLandlordViewModelBuilderTests {
    @Nested
    inner class LandlordViewTests {
        val loggedInLandlord =
            MockLandlordData.createIndividualLandlord(
                baseUser = MockLandlordData.createPrsdbUser("current-user"),
                name = "John Smith",
                email = "john@example.com",
                registrationNumber = RegistrationNumber(RegistrationNumberType.LANDLORD, 1234L),
            )

        @Test
        fun `with single landlord returns one card with LRN and email`() {
            // Arrange, Act
            val cards =
                PropertyDetailsLandlordViewModelBuilder.buildSummaryCards(setOf(loggedInLandlord), loggedInLandlord, 1L)

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
        fun `with multiple landlords returns a card per landlord starting with the logged in landlord with a (you) suffix`() {
            // Arrange
            val landlordList =
                setOf(
                    MockLandlordData.createIndividualLandlord(
                        baseUser = MockLandlordData.createPrsdbUser("other-user"),
                        name = "Alice Band",
                    ),
                    loggedInLandlord,
                    MockLandlordData.createIndividualLandlord(
                        baseUser = MockLandlordData.createPrsdbUser("other-user-2"),
                        name = "Zack Anderson",
                    ),
                )

            // Act
            val cards = PropertyDetailsLandlordViewModelBuilder.buildSummaryCards(landlordList, loggedInLandlord, 1L)

            // Assert
            assertEquals(3, cards.size)
            assertEquals("propertyDetails.landlordDetails.registeredLandlords.currentUserCardTitle", cards[0].title)
            assertEquals("John Smith", cards[0].cardNumber)
            assertEquals("Alice Band", cards[1].title)
            assertEquals("Zack Anderson", cards[2].title)
        }

        @Test
        fun `with org landlord as current landlord uses org card title`() {
            val orgDisplayEmail = "info@acme.com"
            val soleUserContactEmail = "owner@acme.com"
            val orgLandlord =
                MockLandlordData.createOrgLandlord(
                    name = "ACME Properties Ltd",
                    email = orgDisplayEmail,
                    registrantEmail = soleUserContactEmail,
                    registrationNumber = RegistrationNumber(RegistrationNumberType.LANDLORD, 5678L),
                )

            val cards = PropertyDetailsLandlordViewModelBuilder.buildSummaryCards(setOf(orgLandlord), orgLandlord, 1L)
            val displayedEmail = cards[0].summaryList[1].fieldValue

            assertEquals(1, cards.size)
            assertEquals("propertyDetails.landlordDetails.registeredLandlords.currentOrgCardTitle", cards[0].title)
            assertEquals("ACME Properties Ltd", cards[0].cardNumber)
            assertEquals(orgDisplayEmail, displayedEmail)
            assertNotEquals(soleUserContactEmail, displayedEmail)
        }

        @Test
        fun `with mixed individual and org landlords sorts correctly`() {
            val orgLandlord =
                MockLandlordData.createOrgLandlord(
                    name = "ACME Properties Ltd",
                    email = "info@acme.com",
                )

            val landlordList = setOf(loggedInLandlord, orgLandlord)

            val cards = PropertyDetailsLandlordViewModelBuilder.buildSummaryCards(landlordList, orgLandlord, 1L)

            assertEquals(2, cards.size)
            assertEquals("propertyDetails.landlordDetails.registeredLandlords.currentOrgCardTitle", cards[0].title)
            assertEquals("ACME Properties Ltd", cards[0].cardNumber)
            assertEquals("John Smith", cards[1].title)
        }
    }

    @Nested
    inner class LocalCouncilViewTests {
        @Test
        fun `returns cards sorted alphabetically by landlord name`() {
            val landlords =
                setOf(
                    MockLandlordData.createIndividualLandlord(name = "Zoe Adams"),
                    MockLandlordData.createIndividualLandlord(name = "Alice Brown"),
                    MockLandlordData.createIndividualLandlord(name = "Mike Clark"),
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
                MockLandlordData.createIndividualLandlord(
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
            val landlord = MockLandlordData.createIndividualLandlord(name = "John Smith")

            val cards =
                PropertyDetailsLandlordViewModelBuilder.buildLocalCouncilSummaryCards(
                    setOf(landlord),
                    landlordDetailsUrlProvider = { "/local-council/landlord-details/${it.id}" },
                )

            val card = cards.single()
            assertEquals(1, card.actions!!.size)
            assertEquals(
                "propertyDetails.landlordDetails.registeredLandlords.viewLandlordRecord",
                card.actions!![0].text,
            )
            assertEquals("/local-council/landlord-details/${landlord.id}", card.actions!![0].url)
            assertEquals(true, card.actions!![0].opensInNewTab)
        }

        @Test
        fun `org landlord card has title of org name and two rows LRN and whole-org email`() {
            val orgLandlord =
                MockLandlordData.createOrgLandlord(
                    name = "ACME Properties Ltd",
                    email = "info@acme.com",
                    registrationNumber = RegistrationNumber(RegistrationNumberType.LANDLORD, 9012L),
                )

            val cards =
                PropertyDetailsLandlordViewModelBuilder.buildLocalCouncilSummaryCards(
                    setOf(orgLandlord),
                    landlordDetailsUrlProvider = { "/local-council/landlord-details/${it.id}" },
                )

            val card = cards.single()
            assertEquals("ACME Properties Ltd", card.title)
            assertEquals(2, card.summaryList.size)
            assertEquals("landlordDetails.personalDetails.lrn", card.summaryList[0].fieldHeading)
            assertEquals(9012L, (card.summaryList[0].fieldValue as RegistrationNumberDataModel).number)
            assertEquals("landlordDetails.personalDetails.emailAddress", card.summaryList[1].fieldHeading)
            assertEquals("info@acme.com", card.summaryList[1].fieldValue)
        }

        @Test
        fun `org landlord card has a view landlord record action that opens in new tab`() {
            val orgLandlord = MockLandlordData.createOrgLandlord(name = "ACME Properties Ltd", email = "info@acme.com")

            val cards =
                PropertyDetailsLandlordViewModelBuilder.buildLocalCouncilSummaryCards(
                    setOf(orgLandlord),
                    landlordDetailsUrlProvider = { "/local-council/landlord-details/${it.id}" },
                )

            val card = cards.single()
            assertEquals(1, card.actions!!.size)
            assertEquals(
                "propertyDetails.landlordDetails.registeredLandlords.viewLandlordRecord",
                card.actions!![0].text,
            )
            assertEquals("/local-council/landlord-details/${orgLandlord.id}", card.actions!![0].url)
            assertEquals(true, card.actions!![0].opensInNewTab)
        }

        @Test
        fun `mixed individual and org landlords are sorted by name and both keep the view record link`() {
            val individual =
                MockLandlordData.createIndividualLandlord(
                    name = "Zoe Adams",
                    email = "zoe@example.com",
                    phoneNumber = "07700000000",
                )
            val org = MockLandlordData.createOrgLandlord(name = "Acme Ltd", email = "info@acme.com")

            val cards =
                PropertyDetailsLandlordViewModelBuilder.buildLocalCouncilSummaryCards(
                    setOf(individual, org),
                    landlordDetailsUrlProvider = { "/local-council/landlord-details/${it.id}" },
                )

            assertEquals(2, cards.size)
            assertEquals("Acme Ltd", cards[0].title)
            assertEquals(2, cards[0].summaryList.size)
            assertEquals(1, cards[0].actions!!.size)
            assertEquals("Zoe Adams", cards[1].title)
            assertEquals(4, cards[1].summaryList.size)
            assertEquals(1, cards[1].actions!!.size)
        }
    }
}
