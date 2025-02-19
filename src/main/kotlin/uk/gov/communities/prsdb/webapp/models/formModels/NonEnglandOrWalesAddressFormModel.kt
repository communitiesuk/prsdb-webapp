package uk.gov.communities.prsdb.webapp.models.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.LengthConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class NonEnglandOrWalesAddressFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.nonEnglandOrWalesAddress.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.nonEnglandOrWalesAddress.error.tooLong",
                validatorType = LengthConstraintValidator::class,
                validatorArgs = arrayOf("0", "1000"),
            ),
        ],
    )
    var nonEnglandOrWalesAddress: String = ""
}
