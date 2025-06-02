package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

class CheckMatchedEpcFormModel : FormModel {
    // TODO PRSD-1132: add validation message
    @NotNull(message = "forms.epc.error.missing")
    var matchedEpcIsCorrect: Boolean? = null
}
