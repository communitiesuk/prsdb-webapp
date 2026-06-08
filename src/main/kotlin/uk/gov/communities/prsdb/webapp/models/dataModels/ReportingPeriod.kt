package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class ReportingPeriod(
    val start: Instant,
    val end: Instant,
) {
    companion object {
        private val UK_ZONE = ZoneId.of("Europe/London")

        /**
         * Builds an inclusive reporting period for the given date range, interpreted in UK time:
         * the period starts at 00:00:00 on the [from] date and ends at the last instant of the [to] date.
         * If the [to] date is the current date in the UK, the end is the current instant instead of
         * end-of-day, so the period only covers up to "now".
         */
        fun fromDateRange(
            from: LocalDate,
            to: LocalDate,
            clock: Clock = Clock.systemDefaultZone(),
        ): ReportingPeriod {
            val now = clock.instant()
            val todayInUk = DateTimeHelper(clock).getCurrentDateInUK().toJavaLocalDate()

            val start = from.atStartOfDay(UK_ZONE).toInstant()
            val end =
                if (to == todayInUk) {
                    now
                } else {
                    DateTimeHelper.getEndOfDayInstantInUK(to)
                }

            return ReportingPeriod(start, end)
        }
    }
}
