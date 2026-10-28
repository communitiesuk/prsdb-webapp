package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

// TODO PDJB-1959: replace this placeholder form model with the real question's form model
@IsValidPrioritised
class OwnsPropertyFormModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerProperty.ownsProperty.radios.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var ownsProperty: Boolean? = null,
) : FormModel
