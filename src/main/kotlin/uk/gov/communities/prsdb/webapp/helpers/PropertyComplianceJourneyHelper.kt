package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId

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
                    CertificateType.GasSafetyCert -> PropertyComplianceStepId.GasSafetyUpload.urlPathSegment
                    CertificateType.Eicr -> PropertyComplianceStepId.EicrUpload.urlPathSegment
                }
            return getCertFilename(propertyOwnershipId, stepName)
        }
    }
}
