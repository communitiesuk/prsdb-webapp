package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.toNormalizedDecimalString
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.PositiveBigDecimalValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class RentAmountFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.rentAmount.error",
                validatorType = PositiveBigDecimalValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.rentAmount.error",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isNotMoreThanTwoDecimalPlaces",
            ),
        ],
    )
    var rentAmount: String = ""
        set(value) {
            field = value.toNormalizedDecimalString()
        }

    fun isNotMoreThanTwoDecimalPlaces() = rentAmount.toBigDecimal().scale() <= 2
}
