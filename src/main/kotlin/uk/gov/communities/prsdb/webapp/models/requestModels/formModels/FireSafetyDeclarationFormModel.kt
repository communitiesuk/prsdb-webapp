package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

class FireSafetyDeclarationFormModel : FormModel {
    @NotNull(message = "forms.landlordResponsibilities.fireSafety.error.missing")
    var hasDeclared: Boolean? = null
}
