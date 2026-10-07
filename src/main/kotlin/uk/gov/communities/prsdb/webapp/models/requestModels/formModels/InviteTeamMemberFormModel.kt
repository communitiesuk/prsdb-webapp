package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.EmailConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class InviteTeamMemberFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "inviteTeamMember.email.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "inviteTeamMember.email.error.invalidFormat",
                validatorType = EmailConstraintValidator::class,
            ),
        ],
    )
    var emailAddress: String? = null

    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "inviteTeamMember.role.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var role: OrganisationalLandlordUserRole? = null
}
