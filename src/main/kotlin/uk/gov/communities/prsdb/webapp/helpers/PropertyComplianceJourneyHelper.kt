package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.enums.CallbackType
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId

class PropertyComplianceJourneyHelper {
    companion object {
        fun getCertFilename(
            propertyOwnershipId: Long,
            stepName: String,
        ): String = "certificateUpload.$propertyOwnershipId.$stepName"

        fun getCertFilename(
            propertyOwnershipId: Long,
            callbackType: CallbackType,
        ): String {
            val stepName =
                when (callbackType) {
                    CallbackType.GasSafetyCert -> PropertyComplianceStepId.GasSafetyUpload.urlPathSegment
                    CallbackType.Eicr -> PropertyComplianceStepId.EicrUpload.urlPathSegment
                }
            return getCertFilename(propertyOwnershipId, stepName)
        }
    }
}
