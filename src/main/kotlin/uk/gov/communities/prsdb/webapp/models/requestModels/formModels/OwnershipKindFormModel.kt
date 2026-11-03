package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKind
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

// TODO PDJB-1960: replace this placeholder form model with the real question's form model
@IsValidPrioritised
class OwnershipKindFormModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerProperty.ownershipKind.radios.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var ownershipKind: OwnershipKind? = null,
) : FormModel
