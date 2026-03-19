package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.enums.FileCategory
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyCertificateUploadStep

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
                    FileCategory.GasSafetyCert -> GasSafetyCertificateUploadStep.ROUTE_SEGMENT
                    FileCategory.Eicr -> EicrUploadStep.ROUTE_SEGMENT
                }
            return getCertFilename(propertyOwnershipId, stepName)
        }
    }
}
