package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason

class EpcExemptionFormModel : FormModel {
    @NotNull(message = "propertyCompliance.epcTask.epcExemption.error.missing")
    var exemptionReason: EpcExemptionReason? = null
}
