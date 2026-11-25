package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class PropertyRegistrationConfirmMissingComplianceFormModel : ConfirmMissingComplianceFormModel() {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerProperty.confirmMissingCompliance.radios.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    override var wantsToProceed: Boolean? = null
}

@IsValidPrioritised
class ReactivationConfirmMissingComplianceFormModel : ConfirmMissingComplianceFormModel() {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "reactivateProperty.confirmMissingCompliance.radios.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    override var wantsToProceed: Boolean? = null
}

abstract class ConfirmMissingComplianceFormModel : FormModel {
    abstract var wantsToProceed: Boolean?
}
