package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DigitsOnlyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.LengthConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class OrgCharityNumberEnglandAndWalesFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.orgCharityNumberEnglandAndWales.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.orgCharityNumberEnglandAndWales.error.nonNumeric",
                validatorType = DigitsOnlyConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.orgCharityNumberEnglandAndWales.error.length",
                validatorType = LengthConstraintValidator::class,
                validatorArgs = ["7", "8"],
            ),
        ],
    )
    var charityNumber: String? = null
}
