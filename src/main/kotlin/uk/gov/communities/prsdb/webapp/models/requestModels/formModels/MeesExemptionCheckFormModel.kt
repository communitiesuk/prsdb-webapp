package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

class MeesExemptionCheckFormModel : FormModel {
    @NotNull(message = "forms.meesExemptionCheck.error.missing")
    var propertyHasExemption: Boolean? = null
}
