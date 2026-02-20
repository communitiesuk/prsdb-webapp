package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class SelectPropertyFormModel : FormModel {
    @NotNull(message = "joinProperty.selectProperty.error.missing")
    var property: String? = null
}
