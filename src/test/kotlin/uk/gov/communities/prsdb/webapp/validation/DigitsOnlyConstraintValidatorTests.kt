package uk.gov.communities.prsdb.webapp.validation

import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DigitsOnlyConstraintValidatorTests {
    companion object {
        @JvmStatic
        fun provideValidInputs() =
            arrayOf(
                Named.of("single digit", "1"),
                Named.of("multiple digits", "12345678"),
                Named.of("all zeros", "00000000"),
            )

        @JvmStatic
        fun provideInvalidInputs() =
            arrayOf(
                Named.of("letters", "abcdefgh"),
                Named.of("mixed letters and digits", "abc12345"),
                Named.of("special characters", "123-4567"),
                Named.of("spaces", "1234 5678"),
            )
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideValidInputs")
    fun `isValid returns true for digits-only input`(input: String) {
        assertTrue(DigitsOnlyConstraintValidator().isValid(input))
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidInputs")
    fun `isValid returns false for input containing non-digit characters`(input: String) {
        assertFalse(DigitsOnlyConstraintValidator().isValid(input))
    }
}
