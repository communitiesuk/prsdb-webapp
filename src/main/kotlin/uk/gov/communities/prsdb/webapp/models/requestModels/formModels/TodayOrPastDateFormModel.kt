package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import java.time.LocalDate

@IsValidPrioritised
class TodayOrPastDateFormModel : DateFormModel() {
    @TodayOrPastDateDayValidation
    override var day: String = ""

    @TodayOrPastDateMonthValidation
    override var month: String = ""

    @TodayOrPastDateYearValidation
    override var year: String = ""

    fun isValidDateFromTodayOrPast(): Boolean {
        val date = DateTimeHelper.parseDateOrNull(day, month, year) ?: return true
        val today = DateTimeHelper().getCurrentDateInUK()
        return date <= today
    }

    companion object {
        fun fromDateOrNull(date: LocalDate?): TodayOrPastDateFormModel? =
            date?.let {
                TodayOrPastDateFormModel().apply {
                    day = date.dayOfMonth.toString()
                    month = date.monthValue.toString()
                    year = date.year.toString()
                }
            }
    }
}
