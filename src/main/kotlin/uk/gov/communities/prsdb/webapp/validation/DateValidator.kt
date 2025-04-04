package uk.gov.communities.prsdb.webapp.validation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.yearsUntil
import org.hibernate.validator.internal.constraintvalidators.bv.NotBlankValidator
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper

class DateValidator {
    companion object {
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

        fun isValidDay(day: String): Boolean = day.toIntOrNull() in 1..31

        fun isValidMonth(month: String): Boolean = month.toIntOrNull() in 1..12

        fun isValidYear(year: String): Boolean = year.toIntOrNull() in 1900..2099

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
            // Creating an instance of DateTimeHelper in this method allows us to use spring to properly mock time in our tests
            val dateTimeHelper = DateTimeHelper()
            val dateOfBirth = LocalDate.parse(getFullDateString(day, month, year))
            return dateOfBirth.yearsUntil(dateTimeHelper.getCurrentDateInUK())
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
        ): Boolean =
            isBlank(day) ||
                isBlank(month) ||
                isBlank(year) ||
                !isValidDay(day) ||
                !isValidMonth(month) ||
                !isValidYear(
                    year,
                )
    }
}
