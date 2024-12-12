package uk.gov.communities.prsdb.webapp.helpers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import java.time.Clock

class DateTimeHelper(
    private val clock: Clock = Clock.systemUTC(),
) {
    fun getNowAsLocalDate(): LocalDate {
        val instant = clock.instant().toKotlinInstant().toLocalDateTime(TimeZone.of("Europe/London"))
        return LocalDate(instant.year, instant.month.value, instant.dayOfMonth)
    }
}
