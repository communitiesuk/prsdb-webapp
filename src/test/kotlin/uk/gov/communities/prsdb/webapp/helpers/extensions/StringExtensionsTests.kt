package uk.gov.communities.prsdb.webapp.helpers.extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.toNormalizedDecimalString
import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.toNormalizedIntegerString

class StringExtensionsTests {
    @ParameterizedTest
    @CsvSource(
        "007.5, 7.5",
        "0000000.1, 0.1",
        "00100, 100",
        "00100.50, 100.50",
    )
    fun `toNormalizedDecimalString strips leading zeros`(
        input: String,
        expected: String,
    ) {
        assertEquals(expected, input.toNormalizedDecimalString())
    }

    @ParameterizedTest
    @CsvSource(
        "7.5, 7.5",
        "100, 100",
        "0.1, 0.1",
    )
    fun `toNormalizedDecimalString returns unchanged value when no leading zeros`(
        input: String,
        expected: String,
    ) {
        assertEquals(expected, input.toNormalizedDecimalString())
    }

    @Test
    fun `toNormalizedDecimalString preserves trailing decimal zeros`() {
        assertEquals("7.50", "7.50".toNormalizedDecimalString())
    }

    @ParameterizedTest
    @CsvSource(
        "abc, abc",
        "'', ''",
        "12.34.56, 12.34.56",
    )
    fun `toNormalizedDecimalString returns original string for invalid decimal input`(
        input: String,
        expected: String,
    ) {
        assertEquals(expected, input.toNormalizedDecimalString())
    }

    @ParameterizedTest
    @CsvSource(
        "007, 7",
        "0001, 1",
        "00100, 100",
    )
    fun `toNormalizedIntegerString strips leading zeros`(
        input: String,
        expected: String,
    ) {
        assertEquals(expected, input.toNormalizedIntegerString())
    }

    @ParameterizedTest
    @CsvSource(
        "7, 7",
        "100, 100",
        "1, 1",
    )
    fun `toNormalizedIntegerString returns unchanged value when no leading zeros`(
        input: String,
        expected: String,
    ) {
        assertEquals(expected, input.toNormalizedIntegerString())
    }

    @ParameterizedTest
    @CsvSource(
        "abc, abc",
        "'', ''",
        "12.5, 12.5",
    )
    fun `toNormalizedIntegerString returns original string for invalid integer input`(
        input: String,
        expected: String,
    ) {
        assertEquals(expected, input.toNormalizedIntegerString())
    }
}
