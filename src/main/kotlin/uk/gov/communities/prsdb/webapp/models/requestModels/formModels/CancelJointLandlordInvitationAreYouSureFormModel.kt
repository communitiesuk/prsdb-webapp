package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class CancelJointLandlordInvitationAreYouSureFormModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "cancelJointLandlordInvitation.areYouSure.radios.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var wantsToProceed: Boolean? = null,
) : FormModel
