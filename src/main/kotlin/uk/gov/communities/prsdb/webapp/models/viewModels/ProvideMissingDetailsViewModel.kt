package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

data class ProvideMissingDetailsViewModel(
    val showGasSafetyCertificate: Boolean,
    val showElectricalSafetyCertificate: Boolean,
    val showEpc: Boolean,
    val showLicensingDetails: Boolean,
    val showTenancyDetails: Boolean,
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
                showGasSafetyCertificate = isOccupied && propertyCompliance?.gasSafetyCertProvideLater == true,
                showElectricalSafetyCertificate = isOccupied && propertyCompliance?.electricalSafetyCertProvideLater == true,
                showEpc = isOccupied && propertyCompliance?.epcProvideLater == true,
                showLicensingDetails = propertyOwnership.licenseProvideLater == true,
                showTenancyDetails = propertyOwnership.tenancyProvideLater == true,
            )
        }
    }
}
