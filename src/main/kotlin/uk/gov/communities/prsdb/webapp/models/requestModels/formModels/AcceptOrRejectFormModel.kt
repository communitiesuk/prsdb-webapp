package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class AcceptOrRejectFormModel : FormModel {
    @NotNull(message = "acceptOrRejectJointLandlordInvitation.acceptOrReject.error.missing")
    var isInviteAccepted: Boolean? = null
}
