package uk.gov.communities.prsdb.webapp.helpers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinInstant
import java.time.Instant

class LandlordDeactivationDateHelper {
    companion object {
        fun getDeactivationDateFromCreatedDate(createdDate: Instant): LocalDate =
            DateTimeHelper.get3MonthsFromDate(DateTimeHelper.getDateInUK(createdDate.toKotlinInstant()))
    }
}
