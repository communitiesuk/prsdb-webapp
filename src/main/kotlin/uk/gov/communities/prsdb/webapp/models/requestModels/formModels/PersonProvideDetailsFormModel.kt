package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

// TODO PDJB-1963, PDJB-1972: delete or replace this temporary form model when the real provide details page is built
@IsValidPrioritised
class PersonProvideDetailsFormModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerProperty.person.provideDetails.radios.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var provideDetails: YesOrNo? = null,
) : FormModel
