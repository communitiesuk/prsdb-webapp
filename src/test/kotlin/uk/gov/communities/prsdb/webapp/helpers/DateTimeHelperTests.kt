package uk.gov.communities.prsdb.webapp.helpers

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Clock
import java.time.ZoneId
import kotlin.test.assertEquals

class DateTimeHelperTests {
    companion object {
        @JvmStatic
        fun provideInstants() =
            listOf(
                Named.of("winter morning", LocalDateTime(2023, 12, 1, 0, 0, 0).toInstant(TimeZone.of("Europe/London"))),
                Named.of("winter night", LocalDateTime(2023, 12, 1, 23, 59, 59).toInstant(TimeZone.of("Europe/London"))),
                Named.of("summer morning", LocalDateTime(2023, 6, 1, 0, 0, 0).toInstant(TimeZone.of("Europe/London"))),
                Named.of("summer night", LocalDateTime(2023, 6, 1, 23, 59, 59).toInstant(TimeZone.of("Europe/London"))),
            )

        @JvmStatic
        fun provideInstantsWithLocations(): MutableList<Arguments> {
            val namedDateTimes = provideInstants()

            val timeZoneIDs = listOf("America/Los_Angeles", "Europe/London", "Asia/Tokyo")

            val args = mutableListOf<Arguments>()
            for (namedDateTime in namedDateTimes) {
                for (timeZoneID in timeZoneIDs) {
                    args.add(Arguments.of(namedDateTime, timeZoneID))
                }
            }
            return args
        }

        @JvmStatic
        fun provideBirthDatesAndAges() =
            listOf(
                Arguments.of(Named.of("after birthday", LocalDate(1990, 3, 7)), 34),
                Arguments.of(Named.of("on birthday", LocalDate(1990, 3, 8)), 34),
                Arguments.of(Named.of("before birthday", LocalDate(1990, 3, 9)), 33),
            )

        @JvmStatic
        private fun provideDateStringsAndDates() =
            arrayOf(
                Arguments.of(Named.of("a valid date", "12"), "11", "1990", Named.of("the corresponding date", LocalDate(1990, 11, 12))),
                Arguments.of(Named.of("a valid leap date", "29"), "02", "2004", Named.of("the corresponding date", LocalDate(2004, 2, 29))),
                Arguments.of(Named.of("an invalid date", "31"), "11", "1990", null),
                Arguments.of(Named.of("an invalid leap date", "29"), "02", "2005", null),
            )
    }

    @ParameterizedTest(name = "on a {0} in {1}")
    @MethodSource("provideInstantsWithLocations")
    fun `getCurrentDateInUK returns the current date in the UK`(
        instant: Instant,
        timeZoneID: String,
    ) {
        val expectedDate = instant.toLocalDateTime(TimeZone.of("Europe/London")).date

        val dateTimeHelper = DateTimeHelper(Clock.fixed(instant.toJavaInstant(), ZoneId.of(timeZoneID)))

        assertEquals(expectedDate, dateTimeHelper.getCurrentDateInUK())
    }

    @ParameterizedTest(name = "when {0}")
    @MethodSource("provideBirthDatesAndAges")
    fun `getAgeFromBirthDate returns the expected age`(
        birthDate: LocalDate,
        expectedAge: Int,
    ) {
        val currentDateTime = LocalDateTime(2024, 3, 8, 0, 0, 0)

        val dateTimeHelper =
            DateTimeHelper(
                Clock.fixed(
                    currentDateTime.toInstant(TimeZone.of("Europe/London")).toJavaInstant(),
                    ZoneId.of("Europe/London"),
                ),
            )

        assertEquals(dateTimeHelper.getAgeFromBirthDate(birthDate), expectedAge)
    }

    @ParameterizedTest(name = "for a {0}")
    @MethodSource("provideInstants")
    fun `getDateInUK returns the date in the UK for the instant specified`(instant: Instant) {
        val expectedDate = instant.toLocalDateTime(TimeZone.of("Europe/London")).date

        assertEquals(expectedDate, DateTimeHelper.getDateInUK(instant))
    }

    @ParameterizedTest(name = "{3} for {0}")
    @MethodSource("provideDateStringsAndDates")
    fun `parseDateOrNull returns`(
        day: String,
        month: String,
        year: String,
        expectedDateOrNull: LocalDate?,
    ) {
        assertEquals(DateTimeHelper.parseDateOrNull(day, month, year), expectedDateOrNull)
    }
}
