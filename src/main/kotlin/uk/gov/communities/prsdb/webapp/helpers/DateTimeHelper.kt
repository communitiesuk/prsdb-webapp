package uk.gov.communities.prsdb.webapp.helpers

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearsUntil
import java.time.Clock

class DateTimeHelper(
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun getCurrentDateInUK(): LocalDate {
        val now = clock.instant().toKotlinInstant()
        return getDateInUK(now)
    }

    fun getAgeFromBirthDate(birthDate: LocalDate): Int = birthDate.yearsUntil(getCurrentDateInUK())

    companion object {
        fun getDateInUK(instant: Instant): LocalDate {
            val dateTimeInUK = instant.toLocalDateTime(TimeZone.of("Europe/London"))
            return dateTimeInUK.date
        }

        fun parseDateOrNull(
            day: String,
            month: String,
            year: String,
        ): LocalDate? =
            try {
                LocalDate.parse("$year-${month.padStart(2, '0')}-${day.padStart(2, '0')}")
            } catch (e: IllegalArgumentException) {
                null
            }

        fun get28DaysFromDate(date: LocalDate): LocalDate = date.plus(DatePeriod(days = 28))

        fun isDateInPast(date: LocalDate): Boolean = date < DateTimeHelper().getCurrentDateInUK()
    }
}
