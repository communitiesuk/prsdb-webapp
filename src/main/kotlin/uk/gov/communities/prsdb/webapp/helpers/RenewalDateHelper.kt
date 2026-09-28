package uk.gov.communities.prsdb.webapp.helpers

import java.time.LocalDate
import java.time.Month
import java.time.MonthDay
import java.time.Year

class RenewalDateHelper {
    companion object {
        private val LEAP_DAY = MonthDay.of(Month.FEBRUARY, 29)

        fun getRenewalDate(
            anniversary: MonthDay,
            currentDate: LocalDate = LocalDate.now(DateTimeHelper.UK_ZONE),
        ): LocalDate =
            getLeapAdjustedAnniversaryDate(anniversary, currentDate.year).takeIf { it.isAfter(currentDate) }
                ?: getLeapAdjustedAnniversaryDate(anniversary, currentDate.year + 1)

        private fun getLeapAdjustedAnniversaryDate(
            anniversary: MonthDay,
            year: Int,
        ): LocalDate =
            if (anniversary == LEAP_DAY && !Year.isLeap(year.toLong())) {
                LocalDate.of(year, Month.MARCH, 1)
            } else {
                anniversary.atYear(year)
            }
    }
}
