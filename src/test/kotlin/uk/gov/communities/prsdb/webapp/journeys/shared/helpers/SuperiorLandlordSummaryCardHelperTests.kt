package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createSuperiorLandlord

class SuperiorLandlordSummaryCardHelperTests {
    private val helper = SuperiorLandlordSummaryCardHelper()

    @Test
    fun `getSummaryCards orders cards by superior landlord id`() {
        val superiorLandlords =
            mapOf(
                3 to SuperiorLandlordDataModel(detailsKnown = false),
                1 to SuperiorLandlordDataModel(detailsKnown = true, name = "James Hassan", email = "james@example.com"),
                2 to SuperiorLandlordDataModel(detailsKnown = true, name = "Billie Smith", email = "billie@example.com"),
            )

        val cards = helper.getSummaryCards(superiorLandlords)

        assertEquals(
            listOf("James Hassan", "Billie Smith", "registerProperty.superiorLandlords.list.unknownSuperiorLandlord.title"),
            cards.map { it.title },
        )
    }

    @Test
    fun `persisted superior landlords produce ordered read-only summary cards`() {
        // Arrange
        val propertyOwnership = createPropertyOwnership()
        val later = createSuperiorLandlord(propertyOwnership, name = "Zoe Smith", email = "zoe@example.com", id = 2L)
        val earlier = createSuperiorLandlord(propertyOwnership, id = 1L)

        // Act
        val cards = helper.getSummaryCards(listOf(later, earlier))

        // Assert
        assertEquals(
            listOf("registerProperty.superiorLandlords.list.unknownSuperiorLandlord.title", "Zoe Smith"),
            cards.map { it.title },
        )
        assertEquals("registerProperty.superiorLandlords.list.unknownSuperiorLandlord.body", cards[0].bodyTextKey)
        assertTrue(cards[0].summaryList.isEmpty())
        assertEquals("registerProperty.superiorLandlords.list.emailAddress", cards[1].summaryList.single().fieldHeading)
        assertEquals("zoe@example.com", cards[1].summaryList.single().fieldValue)
        assertTrue(cards.all { it.actions.isNullOrEmpty() })
    }

    @Test
    fun `persisted postal-address superior landlord gets property-record note only`() {
        // Arrange
        val propertyOwnership = createPropertyOwnership()
        val postal = createSuperiorLandlord(propertyOwnership, name = "Casey Address")
        val email = createSuperiorLandlord(propertyOwnership, name = "Jordan Email", email = "jordan@example.com")
        val unknown = createSuperiorLandlord(propertyOwnership)

        // Act
        val cards = helper.getSummaryCards(listOf(postal, email, unknown)).associateBy { it.title }

        // Assert
        assertEquals(
            "propertyDetails.peopleResponsible.superiorLandlord.postalAddressNote",
            cards.getValue("Casey Address").bodyTextKey,
        )
        assertTrue(cards.getValue("Casey Address").summaryList.isEmpty())
        assertNull(cards.getValue("Jordan Email").bodyTextKey)
        assertEquals(
            "registerProperty.superiorLandlords.list.unknownSuperiorLandlord.body",
            cards.getValue("registerProperty.superiorLandlords.list.unknownSuperiorLandlord.title").bodyTextKey,
        )
    }

    @Test
    fun `custom unknown details message applies only to unknown superior landlords`() {
        // Arrange
        val propertyOwnership = createPropertyOwnership()
        val unknown = createSuperiorLandlord(propertyOwnership)
        val postal = createSuperiorLandlord(propertyOwnership, name = "Casey Address", id = 2L)
        val email = createSuperiorLandlord(propertyOwnership, name = "Jordan Email", email = "jordan@example.com", id = 3L)

        // Act
        val cards =
            helper
                .getSummaryCards(
                    listOf(unknown, postal, email),
                    SuperiorLandlordSummaryCardHelper.COUNCIL_UNKNOWN_SUPERIOR_LANDLORD_BODY_KEY,
                ).associateBy { it.title }

        // Assert
        assertEquals(
            SuperiorLandlordSummaryCardHelper.COUNCIL_UNKNOWN_SUPERIOR_LANDLORD_BODY_KEY,
            cards.getValue("registerProperty.superiorLandlords.list.unknownSuperiorLandlord.title").bodyTextKey,
        )
        assertEquals(SuperiorLandlordSummaryCardHelper.POSTAL_ADDRESS_NOTE_KEY, cards.getValue("Casey Address").bodyTextKey)
        assertNull(cards.getValue("Jordan Email").bodyTextKey)
    }

    @Test
    fun `superior landlord with a postal address has a card titled with their name and a multi-line postal address row`() {
        val superiorLandlord =
            SuperiorLandlordDataModel(
                detailsKnown = true,
                name = "James Hassan",
                address = AddressDataModel.fromManualAddressData("36 Sunnyside Lane", "Biggleswade", "SG18 9EJ"),
            )

        val card = helper.getSummaryCards(mapOf(1 to superiorLandlord)).single()

        assertEquals("James Hassan", card.title)
        assertEquals("registerProperty.superiorLandlords.list.postalAddress", card.summaryList.single().fieldHeading)
        assertEquals(listOf("36 Sunnyside Lane", "Biggleswade", "SG18 9EJ"), card.summaryList.single().fieldValue)
        assertNull(card.bodyTextKey)
    }

    @Test
    fun `superior landlord with an email has a card titled with their name and an email address row`() {
        val superiorLandlord = SuperiorLandlordDataModel(detailsKnown = true, name = "Billie Smith", email = "email@example.com")

        val card = helper.getSummaryCards(mapOf(1 to superiorLandlord)).single()

        assertEquals("Billie Smith", card.title)
        assertEquals("registerProperty.superiorLandlords.list.emailAddress", card.summaryList.single().fieldHeading)
        assertEquals("email@example.com", card.summaryList.single().fieldValue)
        assertNull(card.bodyTextKey)
    }

    @Test
    fun `superior landlord without known details has an unknown superior landlord card with body text and no rows`() {
        val card = helper.getSummaryCards(mapOf(1 to SuperiorLandlordDataModel(detailsKnown = false))).single()

        assertEquals("registerProperty.superiorLandlords.list.unknownSuperiorLandlord.title", card.title)
        assertEquals("registerProperty.superiorLandlords.list.unknownSuperiorLandlord.body", card.bodyTextKey)
        assertEquals(emptyList<Any>(), card.summaryList)
    }

    @Test
    fun `getSummaryCards uses the actions provided for each superior landlord id`() {
        val superiorLandlords = mapOf(7 to SuperiorLandlordDataModel(detailsKnown = false))

        val card =
            helper
                .getSummaryCards(superiorLandlords) { id ->
                    listOf(SummaryCardActionViewModel(text = "forms.links.change", url = "/edit/$id"))
                }.single()

        assertEquals("/edit/7", card.actions!!.single().url)
    }

    @Test
    fun `getSummaryCards throws when a superior landlord with known details has no name`() {
        val superiorLandlords = mapOf(1 to SuperiorLandlordDataModel(detailsKnown = true, email = "email@example.com"))

        assertThrows<PrsdbWebException> { helper.getSummaryCards(superiorLandlords) }
    }

    @Test
    fun `superior landlord with known details but no email or postal address has a card with the postal address note`() {
        val superiorLandlords = mapOf(1 to SuperiorLandlordDataModel(detailsKnown = true, name = "Billie Smith"))

        val card = helper.getSummaryCards(superiorLandlords).single()

        assertEquals("Billie Smith", card.title)
        assertEquals(emptyList<Any>(), card.summaryList)
        assertEquals(SuperiorLandlordSummaryCardHelper.POSTAL_ADDRESS_NOTE_KEY, card.bodyTextKey)
    }
}
