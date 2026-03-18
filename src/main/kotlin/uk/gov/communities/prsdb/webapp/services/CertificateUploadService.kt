package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.CallbackType
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.VirusScanCallbackRepository

@PrsdbWebService
class CertificateUploadService(
    private val virusScanCallbackRepository: VirusScanCallbackRepository,
    private val fileUploadRepository: FileUploadRepository,
) {
    fun saveCertificateUpload(
        propertyOwnershipId: Long,
        fileUploadId: Long,
        certificateType: CallbackType,
    ): VirusScanCallback {
        val fileUpload = fileUploadRepository.getReferenceById(fileUploadId)

        return virusScanCallbackRepository.save(
            VirusScanCallback(
                upload = fileUpload,
                type = certificateType,
                encodedCallbackData = "$propertyOwnershipId",
            ),
        )
    }
}
