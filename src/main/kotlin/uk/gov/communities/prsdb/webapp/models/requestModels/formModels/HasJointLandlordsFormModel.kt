package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class HasJointLandlordsFormModel : FormModel {
    @NotNull(message = "forms.jointLandlords.hasJointLandlords.error.missing")
    var occupied: Boolean? = null

    companion object {
        fun fromPropertyOwnership(propertyOwnership: PropertyOwnership): HasJointLandlordsFormModel =
            HasJointLandlordsFormModel().apply { occupied = propertyOwnership.isOccupied }
    }
}
