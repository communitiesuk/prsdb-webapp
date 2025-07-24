package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class LicensingTypeFormModel : FormModel {
    @NotNull(message = "forms.licensingType.radios.error.missing")
    var licensingType: LicensingType? = null

    companion object {
        fun fromPropertyOwnership(propertyOwnership: PropertyOwnership): LicensingTypeFormModel =
            LicensingTypeFormModel().apply {
                licensingType = propertyOwnership.license?.licenseType ?: LicensingType.NO_LICENSING
            }
    }
}
