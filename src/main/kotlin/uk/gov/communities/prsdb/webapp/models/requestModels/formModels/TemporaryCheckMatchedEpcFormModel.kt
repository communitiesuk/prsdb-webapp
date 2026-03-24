package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

class TemporaryCheckMatchedEpcFormModel : FormModel {
    @NotNull(message = "forms.checkMatchedEpc.error.missing")
    var checkMatchedEpcMode: String? = null
}
