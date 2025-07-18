package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class SelectLocalAuthorityFormModel : FormModel {
    @NotNull(message = "forms.selectLocalAuthority.error.missing")
    var localAuthorityId: Int? = null
}
