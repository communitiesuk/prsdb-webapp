package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipTypeBeforePdjb955
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class OwnershipTypeFormModel : FormModel {
    @NotNull(message = "forms.ownershipType.radios.error.missing")
    var ownershipType: OwnershipTypeBeforePdjb955? = null

    companion object {
        // TODO PDJB-2016: check whether this form model is still used once the ASSOCIATED_PARTIES
        //  flag is always on / deleted
        fun fromPropertyOwnership(propertyOwnership: PropertyOwnership): OwnershipTypeFormModel =
            OwnershipTypeFormModel().apply { ownershipType = propertyOwnership.ownershipTypeBeforePdjb955 }
    }
}
