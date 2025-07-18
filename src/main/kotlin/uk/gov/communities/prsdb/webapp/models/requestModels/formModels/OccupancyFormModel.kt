package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class OccupancyFormModel : FormModel {
    @NotNull(message = "forms.occupancy.radios.error.missing")
    var occupied: Boolean? = null

    companion object {
        fun fromPropertyOwnership(propertyOwnership: PropertyOwnership): OccupancyFormModel =
            OccupancyFormModel().apply { occupied = propertyOwnership.isOccupied }
    }
}
