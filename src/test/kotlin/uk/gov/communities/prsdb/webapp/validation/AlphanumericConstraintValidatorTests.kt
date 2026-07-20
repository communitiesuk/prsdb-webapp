package uk.gov.communities.prsdb.webapp.validation

import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AlphanumericConstraintValidatorTests {
    companion object {
        @JvmStatic
        fun provideValidCompanyNumbers() =
            arrayOf(
                Named.of("all digits", "12345678"),
                Named.of("leading zeros", "00123456"),
                Named.of("letters and digits", "SC123456"),
                Named.of("all letters", "ABCDEFGH"),
                Named.of("lowercase letters", "sc123456"),
            )

        @JvmStatic
        fun provideInvalidCompanyNumbers() =
            arrayOf(
                Named.of("contains slash", "SC12/345"),
                Named.of("contains asterisk", "1234*678"),
                Named.of("contains plus", "123456+8"),
                Named.of("contains space", "1234 678"),
                Named.of("contains hyphen", "SC-12345"),
            )
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideValidCompanyNumbers")
    fun `isValid returns true when`(input: String) {
        assertTrue(AlphanumericConstraintValidator().isValid(input))
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidCompanyNumbers")
    fun `isValid returns false when`(input: String) {
        assertFalse(AlphanumericConstraintValidator().isValid(input))
    }
}
