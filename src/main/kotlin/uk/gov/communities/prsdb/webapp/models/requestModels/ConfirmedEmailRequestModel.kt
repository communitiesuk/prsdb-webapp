package uk.gov.communities.prsdb.webapp.models.requestModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.EmailConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
open class ConfirmedEmailRequestModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "addLocalCouncilUser.error.missingEmail",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "addLocalCouncilUser.error.notAnEmail",
                validatorType = EmailConstraintValidator::class,
            ),
        ],
    )
    var email: String = "",
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "addLocalCouncilUser.error.noConfirmation",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "addLocalCouncilUser.error.confirmationDoesNotMatch",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isConfirmEmailSameAsEmail",
            ),
        ],
    )
    var confirmEmail: String = "",
) {
    fun isConfirmEmailSameAsEmail(): Boolean = email.trim() == confirmEmail.trim()
}
