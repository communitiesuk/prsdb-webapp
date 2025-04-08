package uk.gov.communities.prsdb.webapp.validation

import org.hibernate.validator.internal.constraintvalidators.bv.NotBlankValidator
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper

class DateValidator {
    companion object {
        fun isAllBlank(
            day: String,
            month: String,
            year: String,
        ): Boolean = isBlank(day) && isBlank(month) && isBlank(year)

        fun isAnyBlank(
            day: String,
            month: String,
            year: String,
        ): Boolean = isBlank(day) || isBlank(month) || isBlank(year)

        fun isBothBlank(
            firstValue: String,
            secondValue: String,
        ): Boolean = isBlank(firstValue) && isBlank(secondValue)

        fun isAnyInvalid(
            day: String,
            month: String,
            year: String,
        ): Boolean = !isValidDay(day) || !isValidMonth(month) || !isValidYear(year)

        fun isValidDay(day: String): Boolean = day.toIntOrNull() in 1..31

        fun isValidMonth(month: String): Boolean = month.toIntOrNull() in 1..12

        fun isValidYear(year: String): Boolean = year.toIntOrNull()?.let { it > 1899 } ?: false

        fun isValidDate(
            day: String,
            month: String,
            year: String,
        ) = DateTimeHelper.parseDateOrNull(day, month, year) != null

        private fun isBlank(value: String): Boolean = !NotBlankValidator().isValid(value as CharSequence?, null)
    }
}
