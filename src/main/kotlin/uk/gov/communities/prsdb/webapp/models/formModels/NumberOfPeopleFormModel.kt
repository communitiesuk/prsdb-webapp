package uk.gov.communities.prsdb.webapp.models.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.PositiveOrZeroIntegerValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class NumberOfPeopleFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.numberOfPeople.input.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.numberOfPeople.input.error.invalidFormat",
                validatorType = PositiveOrZeroIntegerValidator::class,
            ),
        ],
    )
    var numberOfPeople: Int? = null
}
