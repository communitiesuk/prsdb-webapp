package uk.gov.communities.prsdb.webapp.validation

import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailConstraintValidatorTests {
    companion object {
        @JvmStatic
        fun provideValidEmails() =
            arrayOf(
                Named.of("simple address", "test@example.com"),
                Named.of("with dots in local part", "first.last@example.com"),
                Named.of("with plus in local part", "user+tag@example.com"),
                Named.of("with hyphen in domain", "user@my-domain.com"),
                Named.of("with subdomain", "user@mail.example.com"),
                Named.of("with apostrophe in local part", "firstname-o'surname@domain.com"),
                Named.of("with special chars in local part", "user!#\$%&'*+/=?^_`{|}~@example.com"),
                Named.of("single char local part", "a@example.com"),
                Named.of("numeric local part", "123@example.com"),
                Named.of("long TLD", "user@example.museum"),
                Named.of("two letter TLD", "user@example.uk"),
            )

        @JvmStatic
        fun provideInvalidEmails() =
            arrayOf(
                Named.of("null value", null),
                Named.of("miscellaneous whitespace", " \u180e \u200b"),
                Named.of("empty string", ""),
                Named.of("no @ sign", "userexample.com"),
                Named.of("no domain", "user@"),
                Named.of("no local part", "@example.com"),
                Named.of("consecutive dots in local part", "user..name@example.com"),
                Named.of("consecutive dots in domain", "user@example..com"),
                Named.of("domain starts with dot", "user@.example.com"),
                Named.of("no TLD", "user@localhost"),
                Named.of("space in address", "user @example.com"),
                Named.of("obscure whitespace in address", "user\u180e@example.com"),
                Named.of("double quote in local part", "user\"name@example.com"),
                Named.of("semicolon in local part", "user;name@example.com"),
                Named.of("single letter TLD", "user@example.a"),
                Named.of("numeric TLD", "user@example.123"),
                Named.of("multiple @ signs", "user@@example.com"),
                Named.of("domain part over 63 chars", "user@${"a".repeat(64)}.com"),
            )
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideValidEmails")
    fun `isValid returns true for valid email with`(input: String) {
        assertTrue(EmailConstraintValidator().isValid(input))
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidEmails")
    fun `isValid returns false for invalid email with`(input: String?) {
        assertFalse(EmailConstraintValidator().isValid(input))
    }

    @Test
    fun `isValid returns false for email longer than 320 characters`() {
        val longLocal = "a".repeat(310)
        val email = "$longLocal@example.com"
        assertTrue(email.length > 320)
        assertFalse(EmailConstraintValidator().isValid(email))
    }

    @Test
    fun `isValid returns false for hostname longer than 253 characters`() {
        val longHostname = (1..42).joinToString(".") { "abcde" }
        val email = "user@$longHostname.com"
        assertFalse(EmailConstraintValidator().isValid(email))
    }

    @Nested
    inner class OptionalEmailConstraintValidatorTests {
        @Test
        fun `isValid returns true for null`() {
            assertTrue(OptionalEmailConstraintValidator().isValid(null))
        }

        @Test
        fun `isValid returns true for blank string`() {
            assertTrue(OptionalEmailConstraintValidator().isValid(""))
            assertTrue(OptionalEmailConstraintValidator().isValid("   "))
        }

        @Test
        fun `isValid returns true for valid email`() {
            assertTrue(OptionalEmailConstraintValidator().isValid("user@example.com"))
        }

        @Test
        fun `isValid returns false for invalid email`() {
            assertFalse(OptionalEmailConstraintValidator().isValid("not-an-email"))
        }
    }
}
