package uk.gov.communities.prsdb.webapp.models.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class InternationalAddressFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.internationalAddress.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.internationalAddress.error.tooLong",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isInternationalAddressLengthValid",
            ),
        ],
    )
    var internationalAddress: String = ""

    fun isInternationalAddressLengthValid(): Boolean = internationalAddress.length <= 1000
}
