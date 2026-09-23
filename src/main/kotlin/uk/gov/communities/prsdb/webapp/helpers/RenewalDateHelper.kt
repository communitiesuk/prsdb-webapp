package uk.gov.communities.prsdb.webapp.helpers

import java.time.LocalDate
import java.time.Month
import java.time.MonthDay
import java.time.Year

object RenewalDateHelper {
    fun getRenewalDate(
        anniversary: MonthDay,
        referenceYear: Int,
    ): LocalDate {
        val targetYear = referenceYear + 1
        val isLeapDay = anniversary.month == Month.FEBRUARY && anniversary.dayOfMonth == 29
        return if (isLeapDay && !Year.isLeap(targetYear.toLong())) {
            LocalDate.of(targetYear, Month.MARCH, 1)
        } else {
            anniversary.atYear(targetYear)
        }
    }
}
