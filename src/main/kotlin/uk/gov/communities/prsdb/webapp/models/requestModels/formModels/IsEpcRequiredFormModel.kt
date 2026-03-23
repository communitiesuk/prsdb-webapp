package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

class IsEpcRequiredFormModel : FormModel {
    @NotNull(message = "forms.isEpcRequired.error.missing")
    var epcRequired: Boolean? = null
}
