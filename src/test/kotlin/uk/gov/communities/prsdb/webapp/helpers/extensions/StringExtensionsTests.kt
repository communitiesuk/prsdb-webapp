package uk.gov.communities.prsdb.webapp.helpers.extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.containsEmail
import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.isSameEmailAs
import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.toNormalizedCurrencyString
import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.toNormalizedEmail
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

    @ParameterizedTest
    @CsvSource(
        "test@example.com, test@example.com",
        "Test@Example.com, test@example.com",
        "'  test@example.com  ', test@example.com",
        "TEST@EXAMPLE.COM, test@example.com",
    )
    fun `toNormalizedEmail trims and lowercases`(
        input: String,
        expected: String,
    ) {
        assertEquals(expected, input.toNormalizedEmail())
    }

    @ParameterizedTest
    @CsvSource(
        "test@example.com, test@example.com",
        "test@example.com, Test@Example.com",
        "test@example.com, '  TEST@EXAMPLE.COM  '",
    )
    fun `isSameEmailAs returns true for emails differing only by case or surrounding whitespace`(
        email: String,
        other: String,
    ) {
        assertTrue(email.isSameEmailAs(other))
    }

    @Test
    fun `isSameEmailAs returns false for different emails`() {
        assertFalse("test@example.com".isSameEmailAs("other@example.com"))
    }

    @Test
    fun `isSameEmailAs returns false when other is null`() {
        assertFalse("test@example.com".isSameEmailAs(null))
    }

    @Test
    fun `containsEmail returns true when an equivalent email is present ignoring case and whitespace`() {
        val emails = listOf("first@example.com", "Second@Example.com")
        assertTrue(emails.containsEmail("  SECOND@example.com  "))
    }

    @Test
    fun `containsEmail returns false when no equivalent email is present`() {
        val emails = listOf("first@example.com", "second@example.com")
        assertFalse(emails.containsEmail("third@example.com"))
    }
}
