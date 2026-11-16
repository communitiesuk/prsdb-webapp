package uk.gov.communities.prsdb.webapp.helpers

class CertificateFilenameHelper {
    companion object {
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
    }
}
