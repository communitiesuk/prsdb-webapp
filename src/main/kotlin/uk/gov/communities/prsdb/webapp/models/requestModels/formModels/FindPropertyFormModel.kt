package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class FindPropertyFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.findProperty.postcode.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
        ],
    )
    var postcode: String? = null

    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.findProperty.houseNameOrNumber.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
        ],
    )
    var houseNameOrNumber: String? = null
}
