package uk.gov.communities.prsdb.webapp.models.requestModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class InviteLocalCouncilAdminModel : ConfirmedEmailRequestModel() {
    @NotNull(message = "forms.selectLocalAuthority.error.missing")
    var localCouncilId: Int? = null
}
