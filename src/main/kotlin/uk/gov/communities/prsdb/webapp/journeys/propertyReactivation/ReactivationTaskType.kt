package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import kotlinx.serialization.Serializable
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

// The optional reactivation tasks. The order of these entries matters: it is the order the tasks appear in the journey.
@Serializable
enum class ReactivationTaskType {
    LICENSING,
    GAS_SAFETY,
    ELECTRICAL_SAFETY,
    EPC,
    TENANCY_DETAILS,
    ;

    companion object {
        fun requiredFor(propertyOwnership: PropertyOwnership): List<ReactivationTaskType> {
            if (!propertyOwnership.isOccupied) return emptyList()

            val propertyCompliance = propertyOwnership.propertyCompliance
            return entries.filter { taskType ->
                when (taskType) {
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
