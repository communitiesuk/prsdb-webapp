package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

class RentIncludesBillsFormModel : FormModel {
    @NotNull(message = "forms.rentIncludesBills.radios.error.missing")
    var rentIncludesBills: Boolean? = null
}
