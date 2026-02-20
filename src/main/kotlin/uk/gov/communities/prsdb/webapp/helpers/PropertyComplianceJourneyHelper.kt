package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.enums.FileCategory
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId

class PropertyComplianceJourneyHelper {
    companion object {
        fun getCertFilename(
            propertyOwnershipId: Long,
            stepName: String,
        ): String = "certificateUpload.$propertyOwnershipId.$stepName"

        fun getCertFilename(
            propertyOwnershipId: Long,
            fileCategory: FileCategory,
        ): String {
            val stepName =
                when (fileCategory) {
                    FileCategory.GasSafetyCert -> PropertyComplianceStepId.GasSafetyUpload.urlPathSegment
                    FileCategory.Eicr -> PropertyComplianceStepId.EicrUpload.urlPathSegment
                }
            return getCertFilename(propertyOwnershipId, stepName)
        }
    }
}
