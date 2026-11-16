package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel

class FreeholderSummaryCardHelperTests {
    private val helper = FreeholderSummaryCardHelper()

    @Test
    fun `getSummaryCards orders cards by freeholder id`() {
        // Arrange
        val freeholders =
            mapOf(
                3 to FreeholderDataModel(detailsKnown = false),
                1 to FreeholderDataModel(detailsKnown = true, name = "James Hassan", email = "james@example.com"),
                2 to FreeholderDataModel(detailsKnown = true, name = "Billie Smith", email = "billie@example.com"),
            )

        // Act
        val cards = helper.getSummaryCards(freeholders)

        // Assert
        assertEquals(
            listOf("James Hassan", "Billie Smith", "registerProperty.freeholders.list.unknownFreeholder.title"),
            cards.map { it.title },
        )
    }

    @Test
    fun `freeholder with a postal address has a card titled with their name and a multi-line postal address row`() {
        // Arrange
        val freeholder =
            FreeholderDataModel(
                detailsKnown = true,
                name = "James Hassan",
                address = AddressDataModel.fromManualAddressData("36 Sunnyside Lane", "Biggleswade", "SG18 9EJ"),
            )

        // Act
        val card = helper.getSummaryCards(mapOf(1 to freeholder)).single()

        // Assert
        assertEquals("James Hassan", card.title)
        assertEquals("registerProperty.freeholders.list.postalAddress", card.summaryList.single().fieldHeading)
        assertEquals(listOf("36 Sunnyside Lane", "Biggleswade", "SG18 9EJ"), card.summaryList.single().fieldValue)
        assertNull(card.bodyTextKey)
    }

    @Test
    fun `freeholder with an email has a card titled with their name and an email address row`() {
        // Arrange
        val freeholder = FreeholderDataModel(detailsKnown = true, name = "Billie Smith", email = "email@example.com")

        // Act
        val card = helper.getSummaryCards(mapOf(1 to freeholder)).single()

        // Assert
        assertEquals("Billie Smith", card.title)
        assertEquals("registerProperty.freeholders.list.emailAddress", card.summaryList.single().fieldHeading)
        assertEquals("email@example.com", card.summaryList.single().fieldValue)
        assertNull(card.bodyTextKey)
    }

    @Test
    fun `freeholder without known details has an unknown freeholder card with body text and no rows`() {
        // Arrange
        val freeholders = mapOf(1 to FreeholderDataModel(detailsKnown = false))

        // Act
        val card = helper.getSummaryCards(freeholders).single()

        // Assert
        assertEquals("registerProperty.freeholders.list.unknownFreeholder.title", card.title)
        assertEquals("registerProperty.freeholders.list.unknownFreeholder.body", card.bodyTextKey)
        assertEquals(emptyList<Any>(), card.summaryList)
    }

    @Test
    fun `getSummaryCards uses the actions provided for each freeholder id`() {
        // Arrange
        val freeholders = mapOf(7 to FreeholderDataModel(detailsKnown = false))

        // Act
        val card =
            helper
                .getSummaryCards(freeholders) { id ->
                    listOf(SummaryCardActionViewModel(text = "forms.links.change", url = "/edit/$id"))
                }.single()

        // Assert
        assertEquals("/edit/7", card.actions!!.single().url)
    }

    @Test
    fun `getSummaryCards throws when a freeholder with known details has no name`() {
        // Arrange
        val freeholders = mapOf(1 to FreeholderDataModel(detailsKnown = true, email = "email@example.com"))

        // Act & Assert
        assertThrows<PrsdbWebException> { helper.getSummaryCards(freeholders) }
    }

    @Test
    fun `getSummaryCards throws when a freeholder with known details has no email or postal address`() {
        // Arrange
        val freeholders = mapOf(1 to FreeholderDataModel(detailsKnown = true, name = "Billie Smith"))

        // Act & Assert
        assertThrows<PrsdbWebException> { helper.getSummaryCards(freeholders) }
    }
}
