package uk.gov.communities.prsdb.webapp.models.dataModels

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class ReportingPeriod(
    val start: Instant,
    val end: Instant,
) {
    companion object {
        private val UK_ZONE = ZoneId.of("Europe/London")

        /**
         * Builds an inclusive reporting period for the given date range, interpreted in UK time:
         * the period starts at 00:00:00 on the [from] date and ends at 23:59:59 on the [to] date.
         * If the [to] date is the current date in the UK, the end is the current instant instead of
         * end-of-day, so the period only covers up to "now".
         */
        fun fromDateRange(
            from: LocalDate,
            to: LocalDate,
            clock: Clock = Clock.systemDefaultZone(),
        ): ReportingPeriod {
            val now = clock.instant()
            val todayInUk = now.atZone(UK_ZONE).toLocalDate()

            val start = from.atStartOfDay(UK_ZONE).toInstant()
            val end =
                if (to == todayInUk) {
                    now
                } else {
                    to.atTime(LocalTime.of(23, 59, 59)).atZone(UK_ZONE).toInstant()
                }

            return ReportingPeriod(start, end)
        }
    }
}
