package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class ConfirmMissingComplianceFormModel : FormModel {
    @NotNull(message = "registerProperty.confirmMissingCompliance.radios.error.missing")
    var wantsToProceed: Boolean? = null
}
