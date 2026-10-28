package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
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

    @ParameterizedTest
    @MethodSource("provideInvalidAddresses")
    fun `address rejects malformed address lines`(lines: List<String>) {
        // Act & Assert
        assertThrows<IllegalArgumentException> {
            LetterAddressDataModel(lines)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["@", "(", ")", "=", "[", "]", "\"", "\\", "/", ",", "<", ">", "~"])
    fun `address rejects a line starting with any character Notify does not allow at the start of a line`(character: String) {
        // Act & Assert
        assertThrows<IllegalArgumentException> {
            LetterAddressDataModel(listOf("The Occupier", "${character}10 High Street", "SW1A 1AA"))
        }
    }

    @ParameterizedTest
    @MethodSource("provideValidAddresses")
    fun `address accepts valid address lines`(lines: List<String>) {
        // Act & Assert
        assertDoesNotThrow { LetterAddressDataModel(lines) }
    }

    companion object {
        @JvmStatic
        fun provideValidAddresses(): List<Named<List<String>>> =
            listOf(
                Named.of("invalid start characters later in a line", listOf("The Occupier", "Flat 2 (rear), 10 High Street", "SW1A 1AA")),
                Named.of("later line without alphanumeric characters", listOf("The Occupier", "10 High Street", "-", "SW1A 1AA")),
            )

        @JvmStatic
        fun provideInvalidAddresses(): List<Named<List<String>>> =
            listOf(
                Named.of("too few lines", listOf("The Occupier", "SW1A 1AA")),
                Named.of("too many lines", List(8) { "Address line $it" }),
                Named.of("first line without alphanumeric characters", listOf("---", "10 High Street", "SW1A 1AA")),
                Named.of("second line without alphanumeric characters", listOf("The Occupier", "---", "SW1A 1AA")),
                Named.of("line starting with an invalid character", listOf("The Occupier", "10 High Street", "=London", "SW1A 1AA")),
                Named.of(
                    "line starting with an invalid character after whitespace",
                    listOf("The Occupier", " <10 High Street", "SW1A 1AA"),
                ),
            )
    }
}
