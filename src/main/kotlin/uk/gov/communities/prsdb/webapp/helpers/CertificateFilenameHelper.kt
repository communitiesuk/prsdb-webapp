package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType

class CertificateFilenameHelper {
    companion object {
        private const val GAS_SAFETY_UPLOAD_STEP_NAME = "gas-safety-certificate-file-upload"
        private const val ELECTRICAL_SAFETY_UPLOAD_STEP_NAME = "electrical-safety-certificate-file-upload"

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

        fun getUploadStepName(certificateType: CertificateType): String =
            when (certificateType) {
                CertificateType.GasSafetyCert -> GAS_SAFETY_UPLOAD_STEP_NAME
                CertificateType.Eicr, CertificateType.Eic -> ELECTRICAL_SAFETY_UPLOAD_STEP_NAME
            }

        private fun randomSuffix(): String {
            val allowedChars = ('a'..'z')
            return String(CharArray(5) { allowedChars.random() })
        }
    }
}
