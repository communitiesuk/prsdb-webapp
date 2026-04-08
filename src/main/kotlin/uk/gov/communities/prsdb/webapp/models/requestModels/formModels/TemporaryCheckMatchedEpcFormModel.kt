package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

// TODO PDJB-664 - remove when no longer used
class TemporaryCheckMatchedEpcFormModel : FormModel {
    @NotNull(message = "forms.checkMatchedEpc.error.missing")
    var checkMatchedEpcMode: String? = null
}
