package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.AlphanumericConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.LengthConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class OrgCharityNumberScotlandFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.orgCharityNumberScotland.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.orgCharityNumberScotland.error.length",
                validatorType = LengthConstraintValidator::class,
                validatorArgs = ["8", "8"],
            ),
            ConstraintDescriptor(
                messageKey = "forms.orgCharityNumberScotland.error.format",
                validatorType = AlphanumericConstraintValidator::class,
            ),
        ],
    )
    var charityNumber: String? = null
}
