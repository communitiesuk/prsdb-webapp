package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

class EicrFormModel : FormModel {
    @NotNull(message = "forms.eicr.error.missing")
    var hasCert: Boolean? = null
}
