package uk.gov.communities.prsdb.webapp.helpers

import java.time.LocalDate
import java.time.Month
import java.time.MonthDay
import java.time.Year

object RenewalDateHelper {
    fun getRenewalDate(
        anniversary: MonthDay,
        from: LocalDate,
    ): LocalDate {
        val anniversaryThisYear = getLeapYearAdjustedDate(anniversary, from.year)
        return if (anniversaryThisYear > from) {
            anniversaryThisYear
        } else {
            getLeapYearAdjustedDate(anniversary, from.year + 1)
        }
    }

    private fun getLeapYearAdjustedDate(
        monthDay: MonthDay,
        year: Int,
    ): LocalDate =
        if (isLeapDay(monthDay) && !Year.isLeap(year.toLong())) {
            LocalDate.of(year, Month.MARCH, 1)
        } else {
            monthDay.atYear(year)
        }

    private fun isLeapDay(monthDay: MonthDay): Boolean = monthDay == MonthDay.of(Month.FEBRUARY, 29)
}
