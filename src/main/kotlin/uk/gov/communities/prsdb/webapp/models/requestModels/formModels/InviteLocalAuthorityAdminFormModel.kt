package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.EmailConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class InviteLocalAuthorityAdminFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.email.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.email.error.invalidFormat",
                validatorType = EmailConstraintValidator::class,
            ),
        ],
    )
    var email: String = ""

    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "addLAUser.error.noConfirmation",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "addLAUser.error.confirmationDoesNotMatch",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isConfirmEmailSameAsEmail",
            ),
        ],
    )
    val confirmEmail: String = ""

    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.selectLocalAuthority.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var localAuthorityId: Int? = null

    fun isConfirmEmailSameAsEmail(): Boolean = email.trim() == confirmEmail.trim()
}
