package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper.Companion.isAfter
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class TodayOrPastDateFormModel : DateFormModel() {
    @ValidatedBy(
        constraints = [
            // Check missing
            ConstraintDescriptor(
                messageKey = "forms.date.error.missingAll",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notAllBlank",
            ),
            ConstraintDescriptor(
                messageKey = "forms.date.error.missingDM",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notDayAndMonthBlank",
            ),
            ConstraintDescriptor(
                messageKey = "forms.date.error.missingDY",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notDayAndYearBlank",
            ),
            ConstraintDescriptor(
                messageKey = "forms.date.error.missingD",
                validatorType = NotBlankConstraintValidator::class,
            ),
            // Check valid
            ConstraintDescriptor(
                messageKey = "forms.date.error.invalidAll",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notAllInvalid",
            ),
            ConstraintDescriptor(
                messageKey = "forms.date.error.invalidDM",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notDayAndMonthInvalid",
            ),
            ConstraintDescriptor(
                messageKey = "forms.date.error.invalidDY",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notDayAndYearInvalid",
            ),
            ConstraintDescriptor(
                messageKey = "forms.date.error.invalidD",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidDay",
            ),
            // Check if real date
            ConstraintDescriptor(
                messageKey = "forms.date.error.invalidFormat",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidDate",
            ),
            // Check if today or past date
            ConstraintDescriptor(
                messageKey = "forms.todayOrPastDate.error.invalidDate",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidDateFromTodayOrPast",
            ),
        ],
    )
    override var day: String = ""

    @ValidatedBy(
        constraints = [
            // Check missing
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notAllBlank",
            ),
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notDayAndMonthBlank",
            ),
            ConstraintDescriptor(
                messageKey = "forms.date.error.missingMY",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notMonthAndYearBlank",
            ),
            ConstraintDescriptor(
                messageKey = "forms.date.error.missingM",
                validatorType = NotBlankConstraintValidator::class,
            ),
            // Check valid
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notAllInvalid",
            ),
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notDayAndMonthInvalid",
            ),
            ConstraintDescriptor(
                messageKey = "forms.date.error.invalidMY",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notMonthAndYearInvalid",
            ),
            ConstraintDescriptor(
                messageKey = "forms.date.error.invalidM",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidMonth",
            ),
            // Check if today or real past date
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidDateFromTodayOrPast",
            ),
        ],
    )
    override var month: String = ""

    @ValidatedBy(
        constraints = [
            // Check missing
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notAllBlank",
            ),
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notDayAndYearBlank",
            ),
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notMonthAndYearBlank",
            ),
            ConstraintDescriptor(
                messageKey = "forms.date.error.missingY",
                validatorType = NotBlankConstraintValidator::class,
            ),
            // Check valid
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notAllInvalid",
            ),
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notDayAndYearInvalid",
            ),
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notMonthAndYearInvalid",
            ),
            ConstraintDescriptor(
                messageKey = "forms.date.error.invalidY",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidYear",
            ),
            // Check if today or real past date
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidDateFromTodayOrPast",
            ),
        ],
    )
    override var year: String = ""

    fun isValidDateFromTodayOrPast(): Boolean {
        val date = DateTimeHelper.parseDateOrNull(day, month, year) ?: return true
        val today = DateTimeHelper().getCurrentDateInUK()
        return !date.isAfter(today)
    }
}
