package uk.gov.communities.prsdb.webapp.helpers

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyCertificateUploadStep
import java.nio.ByteBuffer
import java.security.MessageDigest

class PropertyComplianceJourneyHelper {
    // TODO PDJB-748 Rename this helper class
    companion object {
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
                "${directory()}/certificateUpload.$journeyId.$stepName.$memberId"
            } else {
                "${directory()}/certificateUpload.$journeyId.$stepName.${randomSuffix()}"
            }

        private fun randomSuffix(): String {
            val allowedChars = ('a'..'z')
            return String(CharArray(5) { allowedChars.random() })
        }

        private fun directory(): String {
            val userName = SecurityContextHolder.getContext().authentication.name

            val digest = MessageDigest.getInstance("MD5").digest(userName.toByteArray())

            return ByteBuffer
                .wrap(digest)
                .long
                .toULong()
                .toString(36)
        }

        fun getCertFilename(
            propertyOwnershipId: Long,
            certificateType: CertificateType,
        ): String {
            val stepName =
                when (certificateType) {
                    CertificateType.GasSafetyCert -> GasSafetyCertificateUploadStep.ROUTE_SEGMENT
                    CertificateType.Eicr, CertificateType.Eic -> EicrUploadStep.ROUTE_SEGMENT
                }
            return getCertFilename(propertyOwnershipId, stepName)
        }
    }
}
