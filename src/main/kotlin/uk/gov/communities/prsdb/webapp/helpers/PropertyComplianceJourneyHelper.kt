package uk.gov.communities.prsdb.webapp.helpers

import org.apache.commons.io.FilenameUtils
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId

class PropertyComplianceJourneyHelper {
    companion object {
        fun getCertFilename(
            propertyOwnershipId: Long,
            stepName: String,
            originalFileName: String,
        ): String {
            val certificateType =
                when (stepName) {
                    PropertyComplianceStepId.GasSafetyUpload.urlPathSegment -> "gas_safety_certificate"
                    PropertyComplianceStepId.EicrUpload.urlPathSegment -> "eicr"
                    else -> throw IllegalStateException("Invalid file upload step name: $stepName")
                }
            return "property_${propertyOwnershipId}_$certificateType.${FilenameUtils.getExtension(originalFileName)}"
        }
    }
}
