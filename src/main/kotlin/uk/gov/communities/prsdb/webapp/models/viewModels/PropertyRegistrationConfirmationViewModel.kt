package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

data class PropertyRegistrationConfirmationViewModel(
    val provideMissingDetails: Boolean,
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
        ): PropertyRegistrationConfirmationViewModel {
            val effectiveProvideMissingDetails = provideMissingDetails && !delegatedToLettingAgent
            return PropertyRegistrationConfirmationViewModel(
                provideMissingDetails = effectiveProvideMissingDetails,
                gasSafetyRequired = isOccupied && propertyCompliance?.gasSafetyCertProvideLater == true,
                electricalSafetyRequired = isOccupied && propertyCompliance?.electricalSafetyCertProvideLater == true,
                epcRequired = isOccupied && propertyCompliance?.epcProvideLater == true,
                licenseProvideLater = propertyOwnership.licenseProvideLater == true,
                tenancyProvideLater = propertyOwnership.tenancyProvideLater == true,
            )
        }
    }
}
