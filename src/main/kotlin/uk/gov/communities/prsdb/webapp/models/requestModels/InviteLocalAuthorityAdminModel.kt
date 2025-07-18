package uk.gov.communities.prsdb.webapp.models.requestModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class InviteLocalAuthorityAdminModel : ConfirmedEmailRequestModel() {
    @NotNull(message = "forms.selectLocalAuthority.error.missing")
    var localAuthorityId: Int? = null
}
