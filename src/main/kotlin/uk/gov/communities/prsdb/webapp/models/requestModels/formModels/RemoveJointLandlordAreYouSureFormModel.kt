package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class RemoveJointLandlordAreYouSureFormModel : FormModel {
    @NotNull(message = "jointLandlords.removeJointLandlord.radios.error.missing")
    var wantsToProceed: Boolean? = null
}
