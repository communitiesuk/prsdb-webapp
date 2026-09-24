package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

data class ProvideMissingDetailsViewModel(
    val gasSafetyRequired: Boolean,
    val electricalSafetyRequired: Boolean,
    val epcRequired: Boolean,
    val licenseProvideLater: Boolean,
    val tenancyProvideLater: Boolean,
) {
    companion object {
        fun from(
            isOccupied: Boolean,
            propertyOwnership: PropertyOwnership,
            propertyCompliance: PropertyCompliance?,
            provideMissingDetails: Boolean,
            delegatedToLettingAgent: Boolean,
        ): ProvideMissingDetailsViewModel? {
            if (!provideMissingDetails || delegatedToLettingAgent) {
                return null
            }
            return ProvideMissingDetailsViewModel(
                gasSafetyRequired = isOccupied && propertyCompliance?.gasSafetyCertProvideLater == true,
                electricalSafetyRequired = isOccupied && propertyCompliance?.electricalSafetyCertProvideLater == true,
                epcRequired = isOccupied && propertyCompliance?.epcProvideLater == true,
                licenseProvideLater = propertyOwnership.licenseProvideLater == true,
                tenancyProvideLater = propertyOwnership.tenancyProvideLater == true,
            )
        }
    }
}
