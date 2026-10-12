package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.containsEmail
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.EmailConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class InviteOrganisationalLandlordUserFormModel : FormModel {
    var invitedEmailAddresses: MutableList<String> = mutableListOf()

    var memberEmailAddresses: MutableList<String> = mutableListOf()

    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "inviteOrganisationalLandlordUser.email.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "inviteOrganisationalLandlordUser.email.error.invalidFormat",
                validatorType = EmailConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "inviteOrganisationalLandlordUser.email.error.alreadyInvited",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isEmailNotAlreadyInvitedOrAMember",
            ),
        ],
    )
    var emailAddress: String? = null

    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "inviteOrganisationalLandlordUser.role.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var role: OrganisationalLandlordUserRole? = null

    fun isEmailNotAlreadyInvitedOrAMember(): Boolean {
        val submittedEmail = emailAddress ?: return true
        return !invitedEmailAddresses.containsEmail(submittedEmail) && !memberEmailAddresses.containsEmail(submittedEmail)
    }
}
