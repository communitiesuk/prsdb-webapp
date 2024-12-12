package uk.gov.communities.prsdb.webapp.helpers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Clock
import java.time.ZoneId
import kotlin.test.assertEquals

class DateTimeHelperTests {
    @ParameterizedTest
    @CsvSource(
        // Winter
        "2023,12,1",
        // Summer
        "2023,6,1",
    )
    fun `getNowAsLocalDate returns the correct local date`(
        year: Int,
        month: Int,
        dayOfMonth: Int,
    ) {
        val expectedDate = LocalDate(year, month, dayOfMonth)

        val dateTimeHelper =
            DateTimeHelper(
                Clock.fixed(
                    expectedDate.atStartOfDayIn(TimeZone.of("Europe/London")).toJavaInstant(),
                    ZoneId.of("UTC"),
                ),
            )

        assertEquals(expectedDate, dateTimeHelper.getNowAsLocalDate())
    }
}
