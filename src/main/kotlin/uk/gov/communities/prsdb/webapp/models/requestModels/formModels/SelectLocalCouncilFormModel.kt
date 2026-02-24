package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class SelectLocalCouncilFormModel : FormModel {
    @NotNull(message = "forms.selectLocalCouncil.error.missing")
    var localCouncilId: Int? = null
}
