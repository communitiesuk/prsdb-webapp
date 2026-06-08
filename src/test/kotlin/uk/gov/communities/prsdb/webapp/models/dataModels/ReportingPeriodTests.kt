package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class ReportingPeriodTests {
    private val ukZone = ZoneId.of("Europe/London")

    private fun clockAt(instant: String): Clock = Clock.fixed(java.time.Instant.parse(instant), ZoneOffset.UTC)

    @Test
    fun `start is the start of the from date in UK time`() {
        val clock = clockAt("2025-06-15T10:30:00Z")

        val period =
            ReportingPeriod.fromDateRange(
                from = LocalDate.of(2025, 1, 10),
                to = LocalDate.of(2025, 1, 20),
                clock = clock,
            )

        val expectedStart = LocalDate.of(2025, 1, 10).atStartOfDay(ukZone).toInstant()
        assertEquals(expectedStart, period.start)
    }

    @Test
    fun `end is the end of the to date in UK time when to date is in the past`() {
        val clock = clockAt("2025-06-15T10:30:00Z")

        val period =
            ReportingPeriod.fromDateRange(
                from = LocalDate.of(2025, 1, 10),
                to = LocalDate.of(2025, 1, 20),
                clock = clock,
            )

        val expectedEnd = ZonedDateTime.of(2025, 1, 20, 23, 59, 59, 999_999_999, ukZone).toInstant()
        assertEquals(expectedEnd, period.end)
    }

    @Test
    fun `end is the current instant when the to date is today in UK`() {
        val nowInstant = java.time.Instant.parse("2025-06-15T10:30:00Z")
        val clock = Clock.fixed(nowInstant, ZoneOffset.UTC)

        val period =
            ReportingPeriod.fromDateRange(
                from = LocalDate.of(2025, 6, 1),
                to = LocalDate.of(2025, 6, 15),
                clock = clock,
            )

        assertEquals(nowInstant, period.end)
    }

    @Test
    fun `a single day range spans the start and end of that day`() {
        val clock = clockAt("2025-06-15T10:30:00Z")

        val period =
            ReportingPeriod.fromDateRange(
                from = LocalDate.of(2025, 3, 5),
                to = LocalDate.of(2025, 3, 5),
                clock = clock,
            )

        assertEquals(LocalDate.of(2025, 3, 5).atStartOfDay(ukZone).toInstant(), period.start)
        assertEquals(ZonedDateTime.of(2025, 3, 5, 23, 59, 59, 999_999_999, ukZone).toInstant(), period.end)
    }

    @Test
    fun `start is computed correctly across the spring DST boundary`() {
        val clock = clockAt("2025-06-15T10:30:00Z")

        // UK clocks go forward at 01:00 on 2025-03-30; start of day is still GMT.
        val period =
            ReportingPeriod.fromDateRange(
                from = LocalDate.of(2025, 3, 30),
                to = LocalDate.of(2025, 3, 31),
                clock = clock,
            )

        assertEquals(LocalDate.of(2025, 3, 30).atStartOfDay(ukZone).toInstant(), period.start)
        assertEquals(ZonedDateTime.of(2025, 3, 31, 23, 59, 59, 999_999_999, ukZone).toInstant(), period.end)
    }

    @Test
    fun `end is computed correctly across the autumn DST boundary`() {
        val clock = clockAt("2025-06-15T10:30:00Z")

        // UK clocks go back at 02:00 on 2025-10-26; end of day is in GMT.
        val period =
            ReportingPeriod.fromDateRange(
                from = LocalDate.of(2025, 10, 25),
                to = LocalDate.of(2025, 10, 26),
                clock = clock,
            )

        assertEquals(LocalDate.of(2025, 10, 25).atStartOfDay(ukZone).toInstant(), period.start)
        assertEquals(ZonedDateTime.of(2025, 10, 26, 23, 59, 59, 999_999_999, ukZone).toInstant(), period.end)
    }
}
