package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class ManualAddressFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "addressForms.manualAddress.addressLineOne.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
        ],
    )
    var addressLineOne: String? = null

    var addressLineTwo: String? = null

    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "addressForms.manualAddress.townOrCity.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
        ],
    )
    var townOrCity: String? = null

    var county: String? = null

    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "addressForms.manualAddress.postcode.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
        ],
    )
    var postcode: String? = null

    // TODO: PDJB-2259: Replace this with a normal is null check and reword the below message.
    // TODO: PDJB-2273: Update the constraint with case where country is assumed and reword the above message.
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "addressForms.manualAddress.country.error.missing",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isCountryPresentIfRequired",
            ),
        ],
    )
    var country: String? = null

    // TODO: PDJB-2259: Remove this
    var isInternationalAddressesEnabled: Boolean? = null

    fun isCountryPresentIfRequired(): Boolean = isInternationalAddressesEnabled != true || !country.isNullOrBlank()
}
