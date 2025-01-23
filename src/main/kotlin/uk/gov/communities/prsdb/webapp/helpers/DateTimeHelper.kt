package uk.gov.communities.prsdb.webapp.helpers

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import java.time.Clock
import java.time.OffsetDateTime

class DateTimeHelper(
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun getCurrentDateInUK(): LocalDate {
        val now = clock.instant().toKotlinInstant()
        return getDateInUK(now)
    }

    companion object {
        fun getDateInUK(instant: Instant): LocalDate {
            val dateTimeInUK = instant.toLocalDateTime(TimeZone.of("Europe/London"))
            return LocalDate(dateTimeInUK.year, dateTimeInUK.month.value, dateTimeInUK.dayOfMonth)
        }

        fun getDateInUK(offsetDateTime: OffsetDateTime): LocalDate {
            val dateTimeInUK = offsetDateTime.toInstant().toKotlinInstant().toLocalDateTime(TimeZone.of("Europe/London"))
            return LocalDate(dateTimeInUK.year, dateTimeInUK.month.value, dateTimeInUK.dayOfMonth)
        }
    }
}
