package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.containsEmail
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.EmailConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class FreeholderEmailFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerProperty.freeholders.email.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "registerProperty.freeholders.email.error.invalidFormat",
                validatorType = EmailConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "registerProperty.freeholders.email.error.duplicate",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isEmailNotAlreadyUsedByAnotherFreeholder",
            ),
        ],
    )
    var emailAddress: String? = null

    var existingFreeholderEmails: List<String> = emptyList()

    fun isEmailNotAlreadyUsedByAnotherFreeholder(): Boolean {
        val submittedEmail = emailAddress ?: return true
        return !existingFreeholderEmails.containsEmail(submittedEmail)
    }
}
