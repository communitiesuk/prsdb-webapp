package uk.gov.communities.prsdb.webapp.models.formModels

import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class LicensingTypeFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.licensingType.radios.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var licensingType: LicensingType? = null
}
