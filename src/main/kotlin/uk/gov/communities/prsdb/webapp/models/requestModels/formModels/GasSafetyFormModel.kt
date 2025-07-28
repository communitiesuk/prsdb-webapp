package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class GasSafetyFormModel : FormModel {
    @NotNull(message = "forms.gasSafety.error.missing")
    var hasCert: Boolean? = null
}
