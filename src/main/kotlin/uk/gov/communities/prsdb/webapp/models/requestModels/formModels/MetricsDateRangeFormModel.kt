package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.FromDateDayValidation
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.FromDateMonthValidation
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.FromDateYearValidation
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.ToDateDayValidation
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.ToDateMonthValidation
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.ToDateYearValidation
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import java.time.LocalDate

@IsValidPrioritised
class MetricsDateRangeFormModel : FormModel {
    @FromDateDayValidation
    var fromDay: String = ""

    @FromDateMonthValidation
    var fromMonth: String = ""

    @FromDateYearValidation
    var fromYear: String = ""

    @ToDateDayValidation
    var toDay: String = ""

    @ToDateMonthValidation
    var toMonth: String = ""

    @ToDateYearValidation
    var toYear: String = ""

    private fun fromDateModel() =
        AnyDateFormModel().apply {
            day = fromDay
            month = fromMonth
            year = fromYear
        }

    private fun toDateModel() =
        AnyDateFormModel().apply {
            day = toDay
            month = toMonth
            year = toYear
        }

    fun fromNotAllBlank(): Boolean = fromDateModel().notAllBlank()

    fun fromNotDayAndMonthBlank(): Boolean = fromDateModel().notDayAndMonthBlank()

    fun fromNotDayAndYearBlank(): Boolean = fromDateModel().notDayAndYearBlank()

    fun fromNotMonthAndYearBlank(): Boolean = fromDateModel().notMonthAndYearBlank()

    fun fromNotAllInvalid(): Boolean = fromDateModel().notAllInvalid()

    fun fromNotDayAndMonthInvalid(): Boolean = fromDateModel().notDayAndMonthInvalid()

    fun fromNotDayAndYearInvalid(): Boolean = fromDateModel().notDayAndYearInvalid()

    fun fromNotMonthAndYearInvalid(): Boolean = fromDateModel().notMonthAndYearInvalid()

    fun fromIsValidDay(): Boolean = fromDateModel().isValidDay()

    fun fromIsValidMonth(): Boolean = fromDateModel().isValidMonth()

    fun fromIsValidYear(): Boolean = fromDateModel().isValidYear()

    fun fromIsValidDate(): Boolean = fromDateModel().isValidDate()

    fun toNotAllBlank(): Boolean = toDateModel().notAllBlank()

    fun toNotDayAndMonthBlank(): Boolean = toDateModel().notDayAndMonthBlank()

    fun toNotDayAndYearBlank(): Boolean = toDateModel().notDayAndYearBlank()

    fun toNotMonthAndYearBlank(): Boolean = toDateModel().notMonthAndYearBlank()

    fun toNotAllInvalid(): Boolean = toDateModel().notAllInvalid()

    fun toNotDayAndMonthInvalid(): Boolean = toDateModel().notDayAndMonthInvalid()

    fun toNotDayAndYearInvalid(): Boolean = toDateModel().notDayAndYearInvalid()

    fun toNotMonthAndYearInvalid(): Boolean = toDateModel().notMonthAndYearInvalid()

    fun toIsValidDay(): Boolean = toDateModel().isValidDay()

    fun toIsValidMonth(): Boolean = toDateModel().isValidMonth()

    fun toIsValidYear(): Boolean = toDateModel().isValidYear()

    fun toIsValidDate(): Boolean = toDateModel().isValidDate()

    /**
     * Self-guarding cross-field check: the "to" date must be on or after the "from" date. Returns
     * true (passes) unless both dates are fully valid, complete dates, so it never fires while
     * either date is still blank or fails one of the per-component date validation rules. This
     * mirrors the date validation chain (which, unlike a bare parse, also rejects years that are
     * not greater than 1899), so the range error never stacks on top of a per-component error.
     */
    fun toIsOnOrAfterFromDate(): Boolean {
        val fromDate = fromLocalDateOrNull() ?: return true
        val toDate = toLocalDateOrNull() ?: return true
        return !toDate.isBefore(fromDate)
    }

    fun fromLocalDateOrNull(): LocalDate? = fullyValidLocalDateOrNull(fromDateModel())

    fun toLocalDateOrNull(): LocalDate? = fullyValidLocalDateOrNull(toDateModel())

    private fun fullyValidLocalDateOrNull(dateModel: AnyDateFormModel): LocalDate? {
        // toLocalDateOrNull() returns a date only when all three components are present and form a
        // real calendar date, but it does not enforce the "year must be greater than 1899" rule, so
        // we additionally require isValidYear() (which, once no component is blank, defers to that rule).
        val date = dateModel.toLocalDateOrNull() ?: return null
        return if (dateModel.isValidYear()) date else null
    }
}
