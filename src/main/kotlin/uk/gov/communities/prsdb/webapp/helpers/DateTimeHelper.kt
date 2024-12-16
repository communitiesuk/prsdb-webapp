package uk.gov.communities.prsdb.webapp.helpers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import java.time.Clock

class DateTimeHelper(
    private val clock: Clock = Clock.systemUTC(),
) {
    fun getCurrentDateInUK(): LocalDate {
        val dateTimeInUK = clock.instant().toKotlinInstant().toLocalDateTime(TimeZone.of("Europe/London"))
        return LocalDate(dateTimeInUK.year, dateTimeInUK.month.value, dateTimeInUK.dayOfMonth)
    }
}
