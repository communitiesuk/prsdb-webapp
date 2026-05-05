package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType

class PropertyComplianceJourneyHelper {
    // TODO PDJB-748 Rename this helper class
    companion object {
        private const val EICR_UPLOAD_ROUTE_SEGMENT = "eicr-file-upload"
        private const val GAS_SAFETY_UPLOAD_ROUTE_SEGMENT = "gas-safety-certificate-file-upload"

        fun getCertFilename(
            propertyOwnershipId: Long,
            stepName: String,
        ): String = "certificateUpload.$propertyOwnershipId.$stepName"

        fun getCertFilename(
            journeyId: String,
            stepName: String,
            memberId: String?,
        ): String =
            if (memberId != null) {
                "certificateUpload.$journeyId.$stepName.$memberId"
            } else {
                "certificateUpload.$journeyId.$stepName.${randomSuffix()}"
            }

        private fun randomSuffix(): String {
            val allowedChars = ('a'..'z')
            return String(CharArray(5) { allowedChars.random() })
        }

        fun getCertFilename(
            propertyOwnershipId: Long,
            certificateType: CertificateType,
        ): String {
            val stepName =
                when (certificateType) {
                    CertificateType.GasSafetyCert -> GAS_SAFETY_UPLOAD_ROUTE_SEGMENT
                    CertificateType.Eicr, CertificateType.Eic -> EICR_UPLOAD_ROUTE_SEGMENT
                }
            return getCertFilename(propertyOwnershipId, stepName)
        }
    }
}
