package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.EmailConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

// TODO PDJB-113: validation for already invited landlords
@IsValidPrioritised
class InviteJointLandlordsFormModel : FormModel {
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
        ],
    )
    var emailAddress: String? = null
}
