package uk.gov.communities.prsdb.webapp.models.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class ManualAddressFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.manualAddress.addressLineOne.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
        ],
    )
    var addressLineOne: String? = null

    var addressLineTwo: String? = null

    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.manualAddress.townOrCity.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
        ],
    )
    var townOrCity: String? = null

    var county: String? = null

    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.manualAddress.postcode.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
        ],
    )
    var postcode: String? = null
}