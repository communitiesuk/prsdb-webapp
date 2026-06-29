package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class OrgNameFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.orgName.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
        ],
    )
    var orgName: String? = null
}
