package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DigitsOnlyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.LengthConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class OrgCharityNumberNorthernIrelandFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.orgCharityNumberNorthernIreland.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.orgCharityNumberNorthernIreland.error.nonNumeric",
                validatorType = DigitsOnlyConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.orgCharityNumberNorthernIreland.error.length",
                validatorType = LengthConstraintValidator::class,
                validatorArgs = ["6", "6"],
            ),
        ],
    )
    var charityNumber: String? = null
}
