package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.PositiveIntegerValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class NumberOfPeopleFormModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.numberOfPeople.input.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.numberOfPeople.input.error.invalidFormat",
                validatorType = PositiveIntegerValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.numberOfPeople.input.error.invalidNumber",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isNotLessThanNumberOfHouseholds",
            ),
        ],
    )
    var numberOfPeople: String = "",
    var numberOfHouseholds: String = "",
) : FormModel {
    fun isNotLessThanNumberOfHouseholds(): Boolean = numberOfPeople.toInt() >= numberOfHouseholds.toInt()
}
