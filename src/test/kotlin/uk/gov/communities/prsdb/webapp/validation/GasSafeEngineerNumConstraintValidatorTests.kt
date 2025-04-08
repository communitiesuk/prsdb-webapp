package uk.gov.communities.prsdb.webapp.validation

import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GasSafeEngineerNumConstraintValidatorTests {
    companion object {
        @JvmStatic
        fun provideValidEngineerNums() =
            arrayOf(
                Named.of("expected format", "0123456"),
                Named.of("unexpected format (spaces)", "789 01 23"),
            )

        @JvmStatic
        fun provideInvalidEngineerNums() =
            arrayOf(
                null,
                Named.of("invalid length (too short)", "012345"),
                Named.of("invalid length (too long)", "01234567"),
                Named.of("invalid characters", "ABCDEFG"),
            )
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideValidEngineerNums")
    fun `isValid returns true when valid input has`(input: String) {
        assertTrue(GasSafeEngineerNumConstraintValidator().isValid(input))
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidEngineerNums")
    fun `isValid returns false when valid input is or has`(input: String?) {
        assertFalse(GasSafeEngineerNumConstraintValidator().isValid(input))
    }
}
