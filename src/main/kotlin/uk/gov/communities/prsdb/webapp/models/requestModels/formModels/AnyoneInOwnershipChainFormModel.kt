package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class AnyoneInOwnershipChainFormModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerProperty.superiorLandlords.anyoneInOwnershipChain.radios.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var anyoneInOwnershipChain: Boolean? = null,
) : FormModel
