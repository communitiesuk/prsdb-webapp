package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.CompanyNumberConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.LengthConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class OrgCompanyNumberFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.orgCompanyNumber.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.orgCompanyNumber.error.length",
                validatorType = LengthConstraintValidator::class,
                validatorArgs = ["8", "8"],
            ),
            ConstraintDescriptor(
                messageKey = "forms.orgCompanyNumber.error.invalidCharacters",
                validatorType = CompanyNumberConstraintValidator::class,
            ),
        ],
    )
    var companyNumber: String? = null
}
