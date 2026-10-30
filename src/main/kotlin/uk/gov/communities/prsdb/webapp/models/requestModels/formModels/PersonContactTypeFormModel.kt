package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.constants.enums.PersonContactType
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

// TODO PDJB-1965, PDJB-1974: delete or replace this temporary form model when the real contact type page is built
@IsValidPrioritised
class PersonContactTypeFormModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerProperty.person.contactType.radios.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var contactType: PersonContactType? = null,
) : FormModel
