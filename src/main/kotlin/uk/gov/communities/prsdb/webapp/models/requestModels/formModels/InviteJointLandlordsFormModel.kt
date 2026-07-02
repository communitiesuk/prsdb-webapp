package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.containsEmail
import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.isSameEmailAs
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.EmailConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class InviteJointLandlordsFormModel : FormModel {
    var invitedEmailAddresses: MutableList<String> = mutableListOf()

    var existingLandlordEmails: MutableList<String> = mutableListOf()

    var emailBeingEdited: String? = null

    var loggedInLandlordEmail: String? = null

    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "jointLandlords.inviteJointLandlord.error.invalidEmail",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "jointLandlords.inviteJointLandlord.error.invalidEmail",
                validatorType = EmailConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "jointLandlords.inviteJointLandlord.error.cannotInviteSelf",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isEmailNotLoggedInLandlord",
            ),
            ConstraintDescriptor(
                messageKey = "jointLandlords.inviteJointLandlord.error.alreadyOnProperty",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isEmailNotAlreadyOnProperty",
            ),
            ConstraintDescriptor(
                messageKey = "jointLandlords.inviteJointLandlord.error.alreadyInvited",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isEmailNotAlreadyInvited",
            ),
        ],
    )
    var emailAddress: String? = null

    fun isEmailNotAlreadyInvited(): Boolean {
        val submittedEmail = emailAddress ?: return true
        return submittedEmail.isSameEmailAs(emailBeingEdited) ||
            !invitedEmailAddresses.containsEmail(submittedEmail)
    }

    fun isEmailNotAlreadyOnProperty(): Boolean {
        val submittedEmail = emailAddress ?: return true
        return !existingLandlordEmails.containsEmail(submittedEmail)
    }

    fun isEmailNotLoggedInLandlord(): Boolean {
        val submittedEmail = emailAddress ?: return true
        return !submittedEmail.isSameEmailAs(loggedInLandlordEmail)
    }
}
