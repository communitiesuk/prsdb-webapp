package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.EmailConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class PropertyManagerDetailsFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerProperty.propertyManager.details.name.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
        ],
    )
    var name: String? = null

    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerProperty.propertyManager.details.email.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "registerProperty.propertyManager.details.email.error.invalidFormat",
                validatorType = EmailConstraintValidator::class,
            ),
        ],
    )
    var emailAddress: String? = null
}
