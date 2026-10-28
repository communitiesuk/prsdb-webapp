package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

// TODO PDJB-1965: delete or replace this temporary form model when the real contact type page is built
@IsValidPrioritised
class FreeholderContactTypeFormModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerProperty.freeholders.contactType.radios.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var contactType: FreeholderContactType? = null,
) : FormModel
