package uk.gov.communities.prsdb.webapp.helpers

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DateTimeHelper {
    val clock: Clock = Clock.System

    fun getNowAsLocalDate(): LocalDate {
        val instant = clock.now().toLocalDateTime(TimeZone.of("UTC"))
        return LocalDate(instant.year, instant.month.value, instant.dayOfMonth)
    }
}
