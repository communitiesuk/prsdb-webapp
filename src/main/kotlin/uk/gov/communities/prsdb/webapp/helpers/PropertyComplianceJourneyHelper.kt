package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
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
            certificateType: CertificateType,
        ): String {
            val stepName =
                when (certificateType) {
                    CertificateType.GasSafetyCert -> GasSafetyCertificateUploadStep.ROUTE_SEGMENT
                    CertificateType.Eicr -> EicrUploadStep.ROUTE_SEGMENT
                }
            return getCertFilename(propertyOwnershipId, stepName)
        }
    }
}
