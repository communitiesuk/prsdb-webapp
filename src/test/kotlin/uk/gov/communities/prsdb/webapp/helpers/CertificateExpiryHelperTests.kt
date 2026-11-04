package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate

class CertificateExpiryHelperTests {
    @ParameterizedTest(name = "{1} when issued on {0}")
    @MethodSource("provideGasIssueAndExpiryDates")
    fun `getGasExpiryDate returns 14 months after the issue date`(
        issueDate: LocalDate,
        expectedExpiryDate: LocalDate,
    ) {
        assertEquals(expectedExpiryDate, CertificateExpiryHelper.getGasExpiryDate(issueDate, isGas14MonthExpiryEnabled = true))
    }

    // TODO: PDJB-1926: Remove this test
    @ParameterizedTest(name = "{1} when issued on {0}")
    @MethodSource("provideBefore14MonthsReworkGasIssueAndExpiryDates")
    fun `getGasExpiryDate returns the anniversary of the issue date when 14 month expiry is disabled`(
        issueDate: LocalDate,
        expectedExpiryDate: LocalDate,
    ) {
        assertEquals(expectedExpiryDate, CertificateExpiryHelper.getGasExpiryDate(issueDate, isGas14MonthExpiryEnabled = false))
    }

    @ParameterizedTest(name = "{1} when the current date {0}")
    @MethodSource("provideCurrentDatesForGas")
    fun `isGasIssueDateExpired is`(
        currentDate: LocalDate,
        expectedIsExpired: Boolean,
    ) {
        assertEquals(
            expectedIsExpired,
            CertificateExpiryHelper.isGasIssueDateExpired(GAS_ISSUE_DATE, isGas14MonthExpiryEnabled = true, currentDate),
        )
    }

    // TODO: PDJB-1926: Remove this test
    @ParameterizedTest(name = "{1} when the current date {0}")
    @MethodSource("provideBefore14MonthsReworkCurrentDatesForGas")
    fun `isGasIssueDateExpired with 14 month expiry disabled is`(
        currentDate: LocalDate,
        expectedIsExpired: Boolean,
    ) {
        assertEquals(
            expectedIsExpired,
            CertificateExpiryHelper.isGasIssueDateExpired(GAS_ISSUE_DATE, isGas14MonthExpiryEnabled = false, currentDate),
        )
    }

    @ParameterizedTest(name = "{1} when the current date {0}")
    @MethodSource("provideCurrentDatesForElectricalSafety")
    fun `isEicExpiryDateExpired is`(
        currentDate: LocalDate,
        expectedIsExpired: Boolean,
    ) {
        assertEquals(expectedIsExpired, CertificateExpiryHelper.isEicExpiryDateExpired(EXPIRY_DATE, currentDate))
    }

    @ParameterizedTest(name = "{1} when the current date {0}")
    @MethodSource("provideCurrentDatesForElectricalSafety")
    fun `isEicrExpiryDateExpired is`(
        currentDate: LocalDate,
        expectedIsExpired: Boolean,
    ) {
        assertEquals(expectedIsExpired, CertificateExpiryHelper.isEicrExpiryDateExpired(EXPIRY_DATE, currentDate))
    }

    @ParameterizedTest(name = "{1} when the current date {0}")
    @MethodSource("provideCurrentDatesForEpc")
    fun `isEpcExpiryDateExpired is`(
        currentDate: LocalDate,
        expectedIsExpired: Boolean,
    ) {
        assertEquals(expectedIsExpired, CertificateExpiryHelper.isEpcExpiryDateExpired(EXPIRY_DATE, currentDate))
    }

    companion object {
        private val GAS_ISSUE_DATE = LocalDate.of(2024, 1, 15)
        private val EXPIRY_DATE = LocalDate.of(2025, 6, 1)

        @JvmStatic
        private fun provideGasIssueAndExpiryDates() =
            listOf(
                arguments(LocalDate.of(2024, 1, 15), LocalDate.of(2025, 3, 15)),
                arguments(LocalDate.of(2023, 12, 31), LocalDate.of(2025, 2, 28)),
                arguments(LocalDate.of(2024, 7, 31), LocalDate.of(2025, 9, 30)),
            )

        // TODO: PDJB-1926: Remove this provider
        @JvmStatic
        private fun provideBefore14MonthsReworkGasIssueAndExpiryDates() =
            listOf(
                arguments(LocalDate.of(2024, 1, 15), LocalDate.of(2025, 1, 15)),
                arguments(LocalDate.of(2024, 2, 29), LocalDate.of(2025, 2, 28)),
            )

        @JvmStatic
        private fun provideCurrentDatesForGas() =
            listOf(
                arguments(named("is 13 months after the issue date", LocalDate.of(2025, 2, 15)), false),
                arguments(named("is the day before the expiry date", LocalDate.of(2025, 3, 14)), false),
                arguments(named("is the expiry date", LocalDate.of(2025, 3, 15)), false),
                arguments(named("is the day after the expiry date", LocalDate.of(2025, 3, 16)), true),
            )

        // TODO: PDJB-1926: Remove this provider
        @JvmStatic
        private fun provideBefore14MonthsReworkCurrentDatesForGas() =
            listOf(
                arguments(named("is the day before the expiry date", LocalDate.of(2025, 1, 14)), false),
                arguments(named("is the expiry date", LocalDate.of(2025, 1, 15)), false),
                arguments(named("is the day after the expiry date", LocalDate.of(2025, 1, 16)), true),
                arguments(named("is 13 months after the issue date", LocalDate.of(2025, 2, 15)), true),
            )

        @JvmStatic
        private fun provideCurrentDatesForElectricalSafety() =
            listOf(
                arguments(named("is the day before the expiry date", EXPIRY_DATE.minusDays(1)), false),
                arguments(named("is the expiry date", EXPIRY_DATE), false),
                arguments(named("is the day after the expiry date", EXPIRY_DATE.plusDays(1)), true),
            )

        @JvmStatic
        private fun provideCurrentDatesForEpc() =
            listOf(
                arguments(named("is the day before the expiry date", EXPIRY_DATE.minusDays(1)), false),
                arguments(named("is the expiry date", EXPIRY_DATE), false),
                arguments(named("is the day after the expiry date", EXPIRY_DATE.plusDays(1)), true),
            )
    }
}
