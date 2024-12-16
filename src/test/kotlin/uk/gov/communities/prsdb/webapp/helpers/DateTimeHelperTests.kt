package uk.gov.communities.prsdb.webapp.helpers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
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
        fun provideLocalDateTimes(): MutableList<Arguments> {
            val namedDateTimes =
                listOf(
                    Named.of("winter morning", LocalDateTime(2023, 12, 1, 0, 0, 0)),
                    Named.of("winter night", LocalDateTime(2023, 12, 1, 23, 59, 59)),
                    Named.of("summer morning", LocalDateTime(2023, 6, 1, 0, 0, 0)),
                    Named.of("summer evening", LocalDateTime(2023, 6, 1, 23, 59, 59)),
                )

            val timeZoneIDs = listOf("America/Los_Angeles", "Europe/London", "Asia/Tokyo")

            val args = mutableListOf<Arguments>()
            for (namedDateTime in namedDateTimes) {
                for (timeZoneID in timeZoneIDs) {
                    args.add(Arguments.of(namedDateTime, timeZoneID))
                }
            }
            return args
        }
    }

    @ParameterizedTest(name = "on a {0} in {1}")
    @MethodSource("provideLocalDateTimes")
    fun `getCurrentDateInUK returns the current date in the UK`(
        localDateTime: LocalDateTime,
        timeZoneID: String,
    ) {
        val expectedDate = LocalDate(localDateTime.year, localDateTime.monthNumber, localDateTime.dayOfMonth)

        val dateTimeHelper =
            DateTimeHelper(
                Clock.fixed(
                    localDateTime.toInstant(TimeZone.of("Europe/London")).toJavaInstant(),
                    ZoneId.of(timeZoneID),
                ),
            )

        assertEquals(expectedDate, dateTimeHelper.getCurrentDateInUK())
    }
}
