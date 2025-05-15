package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason

class EpcExemptionReasonFormModel : FormModel {
    @NotNull(message = "forms.epcExemptionReason.missing")
    var exemptionReason: EpcExemptionReason? = null
}
