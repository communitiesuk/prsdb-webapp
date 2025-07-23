package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class OwnershipTypeFormModel : FormModel {
    @NotNull(message = "forms.ownershipType.radios.error.missing")
    var ownershipType: OwnershipType? = null

    companion object {
        fun fromPropertyOwnership(propertyOwnership: PropertyOwnership): OwnershipTypeFormModel =
            OwnershipTypeFormModel().apply { ownershipType = propertyOwnership.ownershipType }
    }
}
