package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.EmailConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class ConfirmedEmailDataModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(messageKey = "addLAUser.error.missingEmail", validatorType = NotBlankConstraintValidator::class),
            ConstraintDescriptor(messageKey = "addLAUser.error.notAnEmail", validatorType = EmailConstraintValidator::class),
        ],
    )
    val email: String = "",
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(messageKey = "addLAUser.error.noConfirmation", validatorType = NotBlankConstraintValidator::class),
            ConstraintDescriptor(
                messageKey = "addLAUser.error.confirmationDoesNotMatch",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isConfirmEmailSameAsEmail",
            ),
        ],
    )
    val confirmEmail: String = "",
) {
    fun isConfirmEmailSameAsEmail(): Boolean = email.trim() == confirmEmail.trim()
}
