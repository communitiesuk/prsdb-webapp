package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.EmailConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class EmailFormModel : FormModel {
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
    var emailAddress: String? = null

    companion object {
        fun fromLandlord(landlord: Landlord): EmailFormModel = EmailFormModel().apply { emailAddress = landlord.email }

        fun fromLaInvitation(invitation: LocalAuthorityInvitation): EmailFormModel =
            EmailFormModel().apply {
                emailAddress =
                    invitation.invitedEmail
            }
    }
}
