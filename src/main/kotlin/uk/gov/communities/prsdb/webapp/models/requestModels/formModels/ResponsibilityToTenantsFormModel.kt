package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.TrueConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class ResponsibilityToTenantsFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.landlordResponsibilities.responsibilityToTenants.checkbox.error.missing",
                validatorType = TrueConstraintValidator::class,
            ),
        ],
    )
    var agreesToResponsibility: Boolean = false
}
