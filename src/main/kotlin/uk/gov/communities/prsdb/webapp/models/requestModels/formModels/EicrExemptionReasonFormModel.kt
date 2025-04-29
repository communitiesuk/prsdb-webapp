package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class EicrExemptionReasonFormModel : FormModel {
    @NotNull(message = "forms.eicrExemptionReason.missing")
    var exemptionReason: EicrExemptionReason? = null
}
