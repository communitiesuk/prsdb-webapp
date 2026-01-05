package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class FurnishedFormModel : FormModel {
    @NotNull(message = "forms.isThePropertyFurnished.radios.error.missing")
    var furnishedStatus: FurnishedStatus? = null

    companion object {
        fun fromPropertyOwnership(propertyOwnership: PropertyOwnership): FurnishedFormModel =
            FurnishedFormModel().apply { furnishedStatus = propertyOwnership.furnishedStatus }
    }
}
