package uk.gov.communities.prsdb.webapp.validation

import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PositiveBigDecimalValidatorTests {
    companion object {
        @JvmStatic
        fun provideValidValues() =
            arrayOf(
                Named.of("integer", "600"),
                Named.of("decimal", "193.54"),
                Named.of("small value", "0.01"),
                Named.of("just below max", "9999999.99"),
                Named.of("large integer below max", "9999999"),
            )

        @JvmStatic
        fun provideInvalidValues() =
            arrayOf(
                Named.of("exactly 10000000", "10000000"),
                Named.of("above 10000000", "10000001"),
                Named.of("large number above max", "99999999.99"),
                Named.of("zero", "0"),
                Named.of("negative", "-1"),
                Named.of("non-numeric", "abc"),
                Named.of("null value", null),
            )
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideValidValues")
    fun `isValid returns true for valid positive values below 10000000`(input: String) {
        assertTrue(PositiveBigDecimalValidator().isValid(input))
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidValues")
    fun `isValid returns false for invalid values`(input: String?) {
        assertFalse(PositiveBigDecimalValidator().isValid(input))
    }
}
