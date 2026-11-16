package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.containsEmail
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.EmailConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class SuperiorLandlordEmailFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerProperty.superiorLandlords.email.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "registerProperty.superiorLandlords.email.error.invalidFormat",
                validatorType = EmailConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "registerProperty.superiorLandlords.email.error.duplicate",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isEmailNotAlreadyUsedByAnotherSuperiorLandlord",
            ),
        ],
    )
    var emailAddress: String? = null

    var existingSuperiorLandlordEmails: List<String> = emptyList()

    fun isEmailNotAlreadyUsedByAnotherSuperiorLandlord(): Boolean {
        val submittedEmail = emailAddress ?: return true
        return !existingSuperiorLandlordEmails.containsEmail(submittedEmail)
    }
}
