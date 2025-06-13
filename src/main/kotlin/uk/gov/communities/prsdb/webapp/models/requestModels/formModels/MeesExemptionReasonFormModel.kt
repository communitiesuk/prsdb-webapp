package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason

class MeesExemptionReasonFormModel : FormModel {
    @NotNull(message = "forms.meesExemptionReason.error.missing")
    var exemptionReason: MeesExemptionReason? = null
}
