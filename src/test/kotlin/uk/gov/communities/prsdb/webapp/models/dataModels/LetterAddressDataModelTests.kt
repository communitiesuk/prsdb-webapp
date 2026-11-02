package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LetterAddressDataModelTests {
    @Test
    fun `address maps three lines to Notify personalisation without padding`() {
        // Arrange
        val address = LetterAddressDataModel(listOf("The Occupier", "10 High Street", "SW1A 1AA"))

        // Act
        val personalisation = address.toPersonalisation()

        // Assert
        assertEquals(
            mapOf(
                "address_line_1" to "The Occupier",
                "address_line_2" to "10 High Street",
                "address_line_3" to "SW1A 1AA",
            ),
            personalisation,
        )
    }

    @Test
    fun `address preserves all seven lines of an address`() {
        // Arrange
        val address =
            LetterAddressDataModel(
                listOf("Example Recipient", "Example Organisation", "Flat 2", "Example House", "10 High Street", "London", "SW1A 1AA"),
            )

        // Act
        val personalisation = address.toPersonalisation()

        // Assert
        assertEquals(
            mapOf(
                "address_line_1" to "Example Recipient",
                "address_line_2" to "Example Organisation",
                "address_line_3" to "Flat 2",
                "address_line_4" to "Example House",
                "address_line_5" to "10 High Street",
                "address_line_6" to "London",
                "address_line_7" to "SW1A 1AA",
            ),
            personalisation,
        )
    }
}
