package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class HasJointLandlordsFormModel : FormModel {
    @NotNull(message = "jointLandlords.hasJointLandlords.error.missing")
    var hasJointLandlords: Boolean? = null
}
