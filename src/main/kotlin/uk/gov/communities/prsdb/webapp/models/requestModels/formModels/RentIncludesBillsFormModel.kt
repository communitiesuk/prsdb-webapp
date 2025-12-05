package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class RentIncludesBillsFormModel : FormModel {
    @NotNull(message = "forms.rentIncludesBills.radios.error.missing")
    var rentIncludesBills: Boolean? = null
}
