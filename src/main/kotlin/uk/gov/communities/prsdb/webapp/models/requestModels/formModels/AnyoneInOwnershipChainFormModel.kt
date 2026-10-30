package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

// TODO PDJB-1971: delete or replace this temporary form model when the real has superior landlords page is built
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
    var anyoneInOwnershipChain: YesOrNo? = null,
) : FormModel
