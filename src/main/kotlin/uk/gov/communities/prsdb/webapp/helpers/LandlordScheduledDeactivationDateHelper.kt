package uk.gov.communities.prsdb.webapp.helpers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinInstant
import java.time.Instant

class LandlordScheduledDeactivationDateHelper {
    companion object {
        fun getScheduledDeactivationDateFromCreatedDate(createdDate: Instant): LocalDate =
            DateTimeHelper.get3MonthsFromDate(DateTimeHelper.getDateInUK(createdDate.toKotlinInstant()))
    }
}
