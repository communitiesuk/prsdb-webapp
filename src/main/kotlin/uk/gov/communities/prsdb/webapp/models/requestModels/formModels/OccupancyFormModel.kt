package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class OccupancyFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.occupancy.radios.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var occupied: Boolean? = null

    companion object {
        fun fromPropertyOwnership(propertyOwnership: PropertyOwnership): OccupancyFormModel =
            OccupancyFormModel().apply { occupied = propertyOwnership.isOccupied }
    }
}
