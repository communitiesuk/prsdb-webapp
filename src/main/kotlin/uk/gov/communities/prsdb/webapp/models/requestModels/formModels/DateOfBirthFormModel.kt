package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DateValidator
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class DateOfBirthFormModel(
    @ValidatedBy(
        constraints = [
            // Check missing
            ConstraintDescriptor(
                messageKey = "forms.dateOfBirth.error.missingAll",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notAllBlank",
            ),
            ConstraintDescriptor(
                messageKey = "forms.dateOfBirth.error.missingDM",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notDayAndMonthBlank",
            ),
            ConstraintDescriptor(
                messageKey = "forms.dateOfBirth.error.missingDY",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notDayAndYearBlank",
            ),
            ConstraintDescriptor(
                messageKey = "forms.dateOfBirth.error.missingD",
                validatorType = NotBlankConstraintValidator::class,
            ),
            // Check valid individual inputs
            ConstraintDescriptor(
                messageKey = "forms.dateOfBirth.error.invalidAll",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notAllInvalid",
            ),
            ConstraintDescriptor(
                messageKey = "forms.dateOfBirth.error.invalidDM",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notDayAndMonthInvalid",
            ),
            ConstraintDescriptor(
                messageKey = "forms.dateOfBirth.error.invalidDY",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notDayAndYearInvalid",
            ),
            ConstraintDescriptor(
                messageKey = "forms.dateOfBirth.error.invalidD",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidDay",
            ),
            // Check if real date
            ConstraintDescriptor(
                messageKey = "forms.dateOfBirth.error.invalidFormat",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidDate",
            ),
            // Check if valid DoB for service
            ConstraintDescriptor(
                messageKey = "forms.dateOfBirth.error.invalidAge",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidDateForMinimumAge",
            ),
            ConstraintDescriptor(
                messageKey = "forms.dateOfBirth.error.invalidDateOfBirth",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidDateForMaximumAge",
            ),
        ],
    )
    var day: String = "",
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
                messageKey = "forms.dateOfBirth.error.missingMY",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notMonthAndYearBlank",
            ),
            ConstraintDescriptor(
                messageKey = "forms.dateOfBirth.error.missingM",
                validatorType = NotBlankConstraintValidator::class,
            ),
            // Check valid individual inputs
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
                messageKey = "forms.dateOfBirth.error.invalidMY",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notMonthAndYearInvalid",
            ),
            ConstraintDescriptor(
                messageKey = "forms.dateOfBirth.error.invalidM",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidMonth",
            ),
            // Check if invalid date or invalid DoB
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidDateOfBirth",
            ),
        ],
    )
    var month: String = "",
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
                messageKey = "forms.dateOfBirth.error.missingY",
                validatorType = NotBlankConstraintValidator::class,
            ),
            // Check valid individual inputs
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
                messageKey = "forms.dateOfBirth.error.invalidY",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidYear",
            ),
            // Check if invalid date or invalid DoB
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidDateOfBirth",
            ),
        ],
    )
    var year: String = "",
) : uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel {
    val dateValidator = DateValidator()

    fun notAllBlank(): Boolean = !(dateValidator.isAllBlank(day, month, year))

    fun notDayAndMonthBlank(): Boolean = !(dateValidator.isBothBlank(day, month))

    fun notDayAndYearBlank(): Boolean = !(dateValidator.isBothBlank(day, year))

    fun notMonthAndYearBlank(): Boolean = !(dateValidator.isBothBlank(month, year))

    fun notAllInvalid(): Boolean = isValidDay() || isValidMonth() || isValidYear()

    fun notDayAndMonthInvalid(): Boolean = isValidDay() || isValidMonth()

    fun notDayAndYearInvalid(): Boolean = isValidDay() || isValidYear()

    fun notMonthAndYearInvalid(): Boolean = isValidMonth() || isValidYear()

    fun isValidDay(): Boolean = dateValidator.isAnyBlank(day, month, year) || dateValidator.isValidDay(day)

    fun isValidMonth(): Boolean = dateValidator.isAnyBlank(day, month, year) || dateValidator.isValidMonth(month)

    fun isValidYear(): Boolean = dateValidator.isAnyBlank(day, month, year) || dateValidator.isValidYear(year)

    fun isDayOrMonthOrYearInvalid(): Boolean = dateValidator.isDayOrMonthOrYearNotValid(day, month, year)

    fun isValidDate(): Boolean {
        if (isDayOrMonthOrYearInvalid()) return true
        return dateValidator.isValidDate(day, month, year)
    }

    fun isValidDateForMinimumAge(): Boolean {
        if (isDayOrMonthOrYearInvalid()) return true
        val age = dateValidator.getAgeFromDate(day, month, year)
        return age >= 18
    }

    fun isValidDateForMaximumAge(): Boolean {
        if (isDayOrMonthOrYearInvalid()) return true
        val age = dateValidator.getAgeFromDate(day, month, year)
        return age <= 120
    }

    fun isValidDateOfBirth(): Boolean = isValidDate() && isValidDateForMinimumAge() && isValidDateForMaximumAge()
}
