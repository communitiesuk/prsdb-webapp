package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

class EicrExemptionFormModel : FormModel {
    @NotNull(message = "forms.eicrExemption.missing")
    var hasExemption: Boolean? = null
}
