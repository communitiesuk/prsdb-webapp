package uk.gov.communities.prsdb.webapp.helpers.extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.toNormalizedCurrencyString
import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.toNormalizedIntegerString

class StringExtensionsTests {
    @ParameterizedTest
    @CsvSource(
        "007.50, 7.50",
        "0000000.10, 0.10",
        "00100.00, 100.00",
        "00100.50, 100.50",
    )
    fun `toNormalizedCurrencyString strips leading zeros`(
        input: String,
        expected: String,
    ) {
        assertEquals(expected, input.toNormalizedCurrencyString())
    }

    @ParameterizedTest
    @CsvSource(
        "7.50, 7.50",
        "100.00, 100.00",
        "0.10, 0.10",
    )
    fun `toNormalizedCurrencyString returns unchanged value when no leading zeros`(
        input: String,
        expected: String,
    ) {
        assertEquals(expected, input.toNormalizedCurrencyString())
    }

    @Test
    fun `toNormalizedCurrencyString adds trailing decimal zeros if less than two decimal places`() {
        assertEquals("7.50", "7.5".toNormalizedCurrencyString())
    }

    @Test
    fun `toNormalizedCurrencyString preserves trailing decimal if two decimal places`() {
        assertEquals("7.50", "7.50".toNormalizedCurrencyString())
    }

    @Test
    fun `toNormalizedCurrencyString strips trailing decimal zeros beyond the second digit`() {
        assertEquals("7.50", "7.500".toNormalizedCurrencyString())
    }

    @ParameterizedTest
    @CsvSource(
        "abc, abc",
        "'', ''",
        "12.34.56, 12.34.56",
    )
    fun `toNormalizedCurrencyString returns original string for invalid decimal input`(
        input: String,
        expected: String,
    ) {
        assertEquals(expected, input.toNormalizedCurrencyString())
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
