package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AnyDateDayValidation
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AnyDateMonthValidation
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AnyDateYearValidation
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class LeadTrusteeDobFormModel : DateFormModel() {
    @AnyDateDayValidation
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.leadTrusteeDob.error.invalidAge",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidForMinimumAge",
            ),
        ],
    )
    override var day: String = ""

    @AnyDateMonthValidation
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidForMinimumAge",
            ),
        ],
    )
    override var month: String = ""

    @AnyDateYearValidation
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isValidForMinimumAge",
            ),
        ],
    )
    override var year: String = ""

    fun isValidForMinimumAge(): Boolean {
        val dateOfBirth = DateTimeHelper.parseDateOrNull(day, month, year) ?: return true
        val age = DateTimeHelper().getAgeFromBirthDate(dateOfBirth)
        return age >= 16
    }
}
