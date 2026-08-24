package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.constants.enums.WhoProvidesRentalDetails
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

// TODO PDJB-1388: replace placeholder labels/content with the real "who will provide
// these details" page. This form model is a skeleton to drive routing only.
@IsValidPrioritised
class WhoProvidesRentalDetailsFormModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerProperty.whoProvidesRentalDetails.radios.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var whoProvides: WhoProvidesRentalDetails? = null,
) : FormModel
