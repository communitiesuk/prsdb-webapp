package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

interface MatchedEpcFormModel : FormModel {
    var matchedEpcIsCorrect: Boolean?
}

class CheckMatchedEpcFormModel : MatchedEpcFormModel {
    @NotNull(message = "forms.checkMatchedEpc.error.missing")
    override var matchedEpcIsCorrect: Boolean? = null
}
