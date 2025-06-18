package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

class CheckMatchedEpcFormModel : FormModel {
    @NotNull(message = "forms.checkMatchedEpc.error.missing")
    var matchedEpcIsCorrect: Boolean? = null
}
