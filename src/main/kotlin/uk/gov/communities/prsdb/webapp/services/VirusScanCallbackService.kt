package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.CallbackType
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.VirusScanCallbackRepository

@PrsdbWebService
class VirusScanCallbackService(
    private val virusScanCallbackRepository: VirusScanCallbackRepository,
    private val fileUploadRepository: FileUploadRepository,
) {
    fun saveEmailToOwner(
        propertyOwnershipId: Long,
        fileUploadId: Long,
        certificateType: CertificateType,
    ): VirusScanCallback {
        val fileUpload = fileUploadRepository.getReferenceById(fileUploadId)

        val data =
            OwnerEmailCallbackData(
                propertyOwnershipId = propertyOwnershipId,
                certificateType = certificateType,
            )

        return virusScanCallbackRepository.save(
            VirusScanCallback(
                upload = fileUpload,
                type = CallbackType.EmailToOwner,
                encodedCallbackData = Json.encodeToString(data),
            ),
        )
    }
}

@Serializable
data class OwnerEmailCallbackData(
    val propertyOwnershipId: Long,
    val certificateType: CertificateType,
)
