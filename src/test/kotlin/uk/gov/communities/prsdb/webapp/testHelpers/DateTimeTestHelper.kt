package uk.gov.communities.prsdb.webapp.testHelpers

import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import java.time.Instant
import java.time.ZonedDateTime

class DateTimeTestHelper {
    companion object {
        // Subtracting 24-hour periods rather than UK calendar days can land on the wrong UK date after a clock change
        fun getInstantDaysAgoInUK(days: Int): Instant =
            ZonedDateTime
                .now(DateTimeHelper.UK_ZONE)
                .minusDays(days.toLong())
                .toInstant()
    }
}
