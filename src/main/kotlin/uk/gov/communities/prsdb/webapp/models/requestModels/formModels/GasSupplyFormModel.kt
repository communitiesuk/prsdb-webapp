package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class GasSupplyFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "propertyCompliance.gasSafetyTask.gasSupply.error.missing",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "hasGasSupplyIsValidForAction",
            ),
        ],
    )
    var hasGasSupply: Boolean? = null

    var action: String? = null

    fun hasGasSupplyIsValidForAction(): Boolean = action == "provideThisLater" || hasGasSupply != null
}
