package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class FurnishedStatusFormModel : FormModel {
    @NotNull(message = "forms.furnishedStatus.radios.error.missing")
    var furnishedStatus: FurnishedStatus? = null

    companion object {
        fun fromPropertyOwnership(propertyOwnership: PropertyOwnership): FurnishedStatusFormModel =
            FurnishedStatusFormModel().apply { furnishedStatus = propertyOwnership.furnishedStatus }
    }
}
