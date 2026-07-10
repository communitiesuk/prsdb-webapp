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
                messageKey = "forms.leadTrusteeDob.error.invalidDate",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isNotInFuture",
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
                targetMethod = "isNotInFuture",
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
                targetMethod = "isNotInFuture",
            ),
        ],
    )
    override var year: String = ""

    fun isNotInFuture(): Boolean {
        val date = DateTimeHelper.parseDateOrNull(day, month, year) ?: return true
        val today = DateTimeHelper().getCurrentDateInUK()
        return date <= today
    }
}
