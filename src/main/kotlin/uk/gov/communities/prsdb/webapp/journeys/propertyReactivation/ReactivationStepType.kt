package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import kotlinx.serialization.Serializable
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

// The optional reactivation steps, in journey order. A step is required if the property is occupied and the landlord
// chose to provide that detail later when registering it.
@Serializable
enum class ReactivationStepType {
    LICENSING,
    GAS_SAFETY,
    ELECTRICAL_SAFETY,
    EPC,
    TENANCY_DETAILS,
    ;

    companion object {
        fun requiredFor(propertyOwnership: PropertyOwnership): List<ReactivationStepType> {
            if (!propertyOwnership.isOccupied) return emptyList()

            val propertyCompliance = propertyOwnership.propertyCompliance
            return entries.filter { stepType ->
                when (stepType) {
                    LICENSING -> propertyOwnership.licenseProvideLater == true
                    GAS_SAFETY -> propertyCompliance?.gasSafetyCertProvideLater == true
                    ELECTRICAL_SAFETY -> propertyCompliance?.electricalSafetyCertProvideLater == true
                    EPC -> propertyCompliance?.epcProvideLater == true
                    TENANCY_DETAILS -> propertyOwnership.tenancyProvideLater == true
                }
            }
        }
    }
}
