package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.EmailConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

// TODO PDJB-113: validation for already invited landlords
@IsValidPrioritised
class InviteJointLandlordsFormModel : FormModel {
    var emailAddresses: MutableList<String> = mutableListOf()

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
                messageKey = "jointLandlords.inviteJointLandlord.error.alreadyInvited",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isEmailNotAlreadyInvited",
            ),
        ],
    )
    var emailAddress: String? = null

    fun isEmailNotAlreadyInvited(): Boolean = emailAddress == null || !emailAddresses.contains(emailAddress)
}
