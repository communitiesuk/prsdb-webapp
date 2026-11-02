package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

// TODO PDJB-1972: delete or replace this temporary form model when the real provide details page is built
@IsValidPrioritised
class SuperiorLandlordProvideDetailsFormModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerProperty.superiorLandlords.provideDetails.radios.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var provideDetails: YesOrNo? = null,
) : FormModel
