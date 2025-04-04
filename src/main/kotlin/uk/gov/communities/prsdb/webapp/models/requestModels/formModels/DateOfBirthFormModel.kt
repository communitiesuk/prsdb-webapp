package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class DateOfBirthFormModel : IDateFormModel {
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
            // Check if real date and valid DoB for service
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidDateOfBirth",
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
            // Check if real date and valid DoB for service
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidDateOfBirth",
            ),
        ],
    )
    override var year: String = ""

    fun isValidDateForMinimumAge(): Boolean {
        val dateOfBirth = DateTimeHelper.parseDateOrNull(day, month, year) ?: return true
        val age = DateTimeHelper().getAgeFromBirthDate(dateOfBirth)
        return age >= 18
    }

    fun isValidDateForMaximumAge(): Boolean {
        val dateOfBirth = DateTimeHelper.parseDateOrNull(day, month, year) ?: return true
        val age = DateTimeHelper().getAgeFromBirthDate(dateOfBirth)
        return age <= 120
    }

    fun isValidDateOfBirth(): Boolean = isValidDate() && isValidDateForMinimumAge() && isValidDateForMaximumAge()

    companion object {
        fun fromLandlord(landlord: Landlord): DateOfBirthFormModel =
            DateOfBirthFormModel().apply {
                day = landlord.dateOfBirth?.dayOfMonth.toString()
                month = landlord.dateOfBirth?.monthValue.toString()
                year = landlord.dateOfBirth?.year.toString()
            }
    }
}
