package uk.gov.communities.prsdb.webapp.validation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.yearsUntil
import org.hibernate.validator.internal.constraintvalidators.bv.NotBlankValidator
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper

class DateValidator {
    fun isAllBlank(
        day: String,
        month: String,
        year: String,
    ): Boolean = isBlank(day) && isBlank(month) && isBlank(year)

    fun isBothBlank(
        firstValue: String,
        secondValue: String,
    ): Boolean = isBlank(firstValue) && isBlank(secondValue)

    fun isAnyBlank(
        day: String,
        month: String,
        year: String,
    ): Boolean = isBlank(day) || isBlank(month) || isBlank(year)

    fun isValidDay(day: String): Boolean =
        try {
            day.toInt() in 1..31
        } catch (e: NumberFormatException) {
            false
        }

    fun isValidMonth(month: String): Boolean =
        try {
            month.toInt() in 1..12
        } catch (e: NumberFormatException) {
            false
        }

    fun isValidYear(year: String): Boolean =
        try {
            year.toInt() in 1900..2099
        } catch (e: NumberFormatException) {
            false
        }

    fun isValidDate(
        day: String,
        month: String,
        year: String,
    ): Boolean {
        try {
            LocalDate.parse(getFullDateString(day, month, year))
            return true
        } catch (e: IllegalArgumentException) {
            return false
        }
    }

    fun getAgeFromDate(
        day: String,
        month: String,
        year: String,
    ): Int {
        // We have to create a new instance of DateTimeHelper in this function
        // This means spring will allow us to properly mock .getNowAsLocalDate() in our tests
        val dateTimeHelper = DateTimeHelper()
        val dateOfBirth = LocalDate.parse(getFullDateString(day, month, year))
        return dateOfBirth.yearsUntil(dateTimeHelper.getNowAsLocalDate())
    }

    private fun isNotBlank(value: Any): Boolean = NotBlankValidator().isValid(value as CharSequence?, null)

    fun isBlank(value: String): Boolean = !isNotBlank(value)

    private fun getFullDateString(
        day: String,
        month: String,
        year: String,
    ): String = "$year-${month.padStart(2, '0')}-${day.padStart(2, '0')}"

    fun isDayOrMonthOrYearNotValid(
        day: String,
        month: String,
        year: String,
    ): Boolean = isBlank(day) || isBlank(month) || isBlank(year) || !isValidDay(day) || !isValidMonth(month) || !isValidYear(year)
}
