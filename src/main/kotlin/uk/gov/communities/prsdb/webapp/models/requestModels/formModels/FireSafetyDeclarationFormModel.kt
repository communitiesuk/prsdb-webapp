package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

class FireSafetyDeclarationFormModel : FormModel {
    @NotNull(message = "forms.epc.error.missing")
    var hasConfirmed: Boolean? = null
}
