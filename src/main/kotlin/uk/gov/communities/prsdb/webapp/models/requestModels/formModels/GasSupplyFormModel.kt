package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class GasSupplyFormModel : FormModel {
    @NotNull(message = "propertyCompliance.gasSafetyTask.gasSupply.error.missing")
    var hasGasSupply: Boolean? = null
}
