package uk.gov.communities.prsdb.webapp.testHelpers

import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import java.time.Instant
import java.time.LocalTime

class DateTimeTestHelper {
    companion object {
        // Subtracting 24-hour days from now can shift the UK calendar date across a clock change, so anchor to UK midday
        fun getMiddayInstantInUKDaysAgo(days: Int): Instant =
            DateTimeHelper()
                .getCurrentDateInUK()
                .toJavaLocalDate()
                .minusDays(days.toLong())
                .atTime(LocalTime.NOON)
                .atZone(DateTimeHelper.UK_ZONE)
                .toInstant()
    }
}
