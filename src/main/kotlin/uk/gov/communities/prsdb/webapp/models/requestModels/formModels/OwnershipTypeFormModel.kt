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
        // TODO PDJB-2016: check whether this form model is still used once the three new pages
        //  ("Do you own this property" PDJB-1959, "What kind of ownership do you have" PDJB-1960
        //  and "Which type of freehold do you have" PDJB-1961) replace the single ownership type
        //  question. The new OwnershipType is derived from all three, so this model is likely to
        //  be unused - if so, delete it along with the flag.
        fun fromPropertyOwnership(propertyOwnership: PropertyOwnership): OwnershipTypeFormModel =
            OwnershipTypeFormModel().apply { ownershipType = propertyOwnership.ownershipTypeBeforePdjb955 }
    }
}
