package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.constants.CUSTOM_BILLS_INCLUDED_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.LengthConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class BillsIncludedFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.billsIncluded.error.missing",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notAllFalse",
            ),
        ],
    )
    var billsIncluded: List<String?>? = null

    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.billsIncluded.error.somethingElse.missing",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isCustomBillsIncludedValidNotBlank",
            ),
            ConstraintDescriptor(
                messageKey = "forms.billsIncluded.error.somethingElse.tooLong",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isCustomBillsIncludedNotTooLong",
            ),
        ],
    )
    var customBillsIncluded: String = ""

    private fun isSomethingElseSelected(): Boolean = billsIncluded?.contains(BillsIncluded.SOMETHING_ELSE.toString()) == true

    fun notAllFalse(): Boolean = billsIncluded?.isNotEmpty() == true

    fun isCustomBillsIncludedValidNotBlank(): Boolean = !isSomethingElseSelected() || customBillsIncluded.isNotBlank()

    fun isCustomBillsIncludedNotTooLong(): Boolean =
        !isSomethingElseSelected() ||
            LengthConstraintValidator("0", CUSTOM_BILLS_INCLUDED_MAX_LENGTH.toString()).isValid(customBillsIncluded)
}
