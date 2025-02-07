package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class AddressHelperTests {
    companion object {
        @JvmStatic
        fun provideUprnStrings() =
            listOf(
                Arguments.of(
                    Named.of("a valid UPRN string (lower boundary)", "1"),
                    Named.of("a UPRN", 1L),
                ),
                Arguments.of(
                    Named.of("a valid UPRN string (upper boundary)", "123456789012"),
                    Named.of("a UPRN", 123456789012L),
                ),
                Arguments.of(
                    Named.of("a non-numeric string", "not-a-uprn"),
                    null,
                ),
                Arguments.of(
                    Named.of("an invalid length string (short)", ""),
                    null,
                ),
                Arguments.of(
                    Named.of("an invalid length string (long)", "1234567890123"),
                    null,
                ),
            )
    }

    @ParameterizedTest(name = "{1} when given {0}")
    @MethodSource("provideUprnStrings")
    fun `parseUPRNOrNull returns`(
        uprnString: String,
        expectedUPRN: Long?,
    ) {
        val uprn = AddressHelper.parseUprnOrNull(uprnString)

        assertEquals(expectedUPRN, uprn)
    }
}
