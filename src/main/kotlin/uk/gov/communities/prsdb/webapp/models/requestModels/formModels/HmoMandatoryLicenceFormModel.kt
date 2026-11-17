package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.LengthConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class HmoMandatoryLicenceFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "licensing.hmoMandatoryLicence.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "licensing.licenceNumber.error.tooLong",
                validatorType = LengthConstraintValidator::class,
                validatorArgs = arrayOf("0", "255"),
            ),
        ],
    )
    var licenceNumber: String? = null

    companion object {
        fun fromPropertyOwnership(propertyOwnership: PropertyOwnership): HmoMandatoryLicenceFormModel =
            HmoMandatoryLicenceFormModel().apply {
                licenceNumber = propertyOwnership.license?.licenseNumber
            }
    }
}
