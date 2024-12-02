package uk.gov.communities.prsdb.webapp.models.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.PositiveOrZeroIntegerValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class NumberOfHouseholdsFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.numberOfHouseholds.input.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.numberOfHouseholds.input.error.invalidFormat",
                validatorType = PositiveOrZeroIntegerValidator::class,
            ),
        ],
    )
    var numberOfHouseholds: Int? = null
}
