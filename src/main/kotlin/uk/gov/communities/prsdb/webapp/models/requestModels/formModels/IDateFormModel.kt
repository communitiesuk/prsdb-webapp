package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.DateValidator

interface IDateFormModel : FormModel {
    var day: String

    var month: String

    var year: String

    fun notAllBlank(): Boolean = !(DateValidator.isAllBlank(day, month, year))

    fun notDayAndMonthBlank(): Boolean = !(DateValidator.isBothBlank(day, month))

    fun notDayAndYearBlank(): Boolean = !(DateValidator.isBothBlank(day, year))

    fun notMonthAndYearBlank(): Boolean = !(DateValidator.isBothBlank(month, year))

    fun notAllInvalid(): Boolean = isValidDay() || isValidMonth() || isValidYear()

    fun notDayAndMonthInvalid(): Boolean = isValidDay() || isValidMonth()

    fun notDayAndYearInvalid(): Boolean = isValidDay() || isValidYear()

    fun notMonthAndYearInvalid(): Boolean = isValidMonth() || isValidYear()

    fun isValidDay(): Boolean = DateValidator.isAnyBlank(day, month, year) || DateValidator.isValidDay(day)

    fun isValidMonth(): Boolean = DateValidator.isAnyBlank(day, month, year) || DateValidator.isValidMonth(month)

    fun isValidYear(): Boolean = DateValidator.isAnyBlank(day, month, year) || DateValidator.isValidYear(year)

    fun isValidDate(): Boolean =
        DateValidator.isAnyBlank(day, month, year) ||
            DateValidator.isAnyInvalid(day, month, year) ||
            DateValidator.isValidDate(day, month, year)
}
