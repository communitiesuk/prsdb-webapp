package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
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
        ],
    )
    var rentAmount: String = ""

    // TODO PDJB-102 rentAmount on PropertyOwnership entity is of type BigDecimal with up to 2 decimal places
//      We'll need a validator - maybe custom? - to validate this is a positive number that is either an integer or has up to 2 decimal places
//      current validator is making the string a BigDecimal and checking it's positive but not checking the decimal places
}
