package uk.gov.communities.prsdb.webapp.helpers

import org.apache.commons.io.FilenameUtils
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.FileNameInfo
import uk.gov.communities.prsdb.webapp.models.dataModels.FileNameInfo.FileCategory
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyUploadCertificateFormModel

class PropertyComplianceJourneyHelper {
    companion object {
        fun getUploadCertificateFormModelClass(stepName: String) =
            when (stepName) {
                PropertyComplianceStepId.GasSafetyUpload.urlPathSegment -> GasSafetyUploadCertificateFormModel::class
                PropertyComplianceStepId.EicrUpload.urlPathSegment -> EicrUploadCertificateFormModel::class
                else -> throw IllegalStateException("Invalid file upload step name: $stepName")
            }

        fun getCertFilename(
            propertyOwnershipId: Long,
            stepName: String,
            originalFileName: String,
        ): String {
            val certificateType =
                when (stepName) {
                    PropertyComplianceStepId.GasSafetyUpload.urlPathSegment -> FileCategory.GasSafetyCert
                    PropertyComplianceStepId.EicrUpload.urlPathSegment -> FileCategory.Eirc
                    else -> throw IllegalStateException("Invalid file upload step name: $stepName")
                }
            return FileNameInfo(propertyOwnershipId, certificateType, FilenameUtils.getExtension(originalFileName)).toString()
        }
    }
}
