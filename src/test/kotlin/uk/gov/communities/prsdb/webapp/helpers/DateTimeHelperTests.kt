package uk.gov.communities.prsdb.webapp.helpers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Clock
import java.time.ZoneId
import kotlin.test.assertEquals

class DateTimeHelperTests {
    @ParameterizedTest
    @CsvSource(
        // Winter morning
        "2023,12,1,0,0,0",
        // Winter night
        "2023,12,1,23,59,59",
        // Summer morning
        "2023,6,1,0,0,0",
        // Summer evening
        "2023,6,1,23,59,59",
    )
    fun `getCurrentDateInUK returns the correct local date`(
        year: Int,
        month: Int,
        dayOfMonth: Int,
        hour: Int,
        minute: Int,
        second: Int,
    ) {
        val expectedDate = LocalDate(year, month, dayOfMonth)
        val expectedDateTime = LocalDateTime(expectedDate, LocalTime(hour, minute, second))

        val dateTimeHelper =
            DateTimeHelper(
                Clock.fixed(
                    expectedDateTime.toInstant(TimeZone.of("Europe/London")).toJavaInstant(),
                    ZoneId.of("UTC"),
                ),
            )

        assertEquals(expectedDate, dateTimeHelper.getCurrentDateInUK())
    }
}
