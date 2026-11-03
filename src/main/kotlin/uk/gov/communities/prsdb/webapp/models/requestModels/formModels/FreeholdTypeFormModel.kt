package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdType
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

// TODO PDJB-1961: replace this placeholder form model with the real question's form model
@IsValidPrioritised
class FreeholdTypeFormModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerProperty.freeholdType.radios.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var freeholdType: FreeholdType? = null,
) : FormModel
