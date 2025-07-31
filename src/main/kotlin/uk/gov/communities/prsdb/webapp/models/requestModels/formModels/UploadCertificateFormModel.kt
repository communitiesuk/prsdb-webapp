package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import org.apache.commons.fileupload2.core.FileItemInput
import org.apache.commons.io.FilenameUtils
import kotlin.math.pow
import kotlin.reflect.KClass

abstract class UploadCertificateFormModel : FormModel {
    abstract val certificate: Nothing?

    var name: String = ""

    var fileUploadId: Long? = null

    var contentType: String = ""

    var contentLength: Long = 0

    var isUploadSuccessfulOrNull: Boolean? = null

    var isUserSubmittedMetadataOnly: Boolean = true

    fun isNameNotBlank() = name.isNotBlank()

    fun isFileTypeValid() = !isNameNotBlank() || FilenameUtils.getExtension(name) in validExtensions && contentType in validMimeTypes

    fun isContentLengthValid() = !isNameNotBlank() || !isFileTypeValid() || contentLength <= maxContentLength

    fun isUploadSuccessfulOrInvalid() = isUploadSuccessfulOrNull != false

    companion object {
        private val validExtensions = listOf("pdf", "png", "jpeg", "jpg")
        val validMimeTypes = listOf("application/pdf", "image/png", "image/jpeg")
        val maxContentLength = 15 * 1024.0.pow(2) // 15MB

        fun fromUploadedFileMetadata(
            desiredClass: KClass<out UploadCertificateFormModel>,
            fileItemInput: FileItemInput,
            fileLength: Long,
        ): UploadCertificateFormModel {
            val uploadCertificateFormModel =
                when (desiredClass) {
                    GasSafetyUploadCertificateFormModel::class -> GasSafetyUploadCertificateFormModel()
                    EicrUploadCertificateFormModel::class -> EicrUploadCertificateFormModel()
                    else -> throw IllegalStateException("Unsupported desired class: ${desiredClass.simpleName}")
                }

            return uploadCertificateFormModel.apply {
                this.name = fileItemInput.name
                this.contentType = fileItemInput.contentType
                this.contentLength = fileLength
                this.isUploadSuccessfulOrNull = null
                this.isUserSubmittedMetadataOnly = false
            }
        }

        fun fromFileItemUpload(
            desiredClass: KClass<out UploadCertificateFormModel>,
            fileItemInput: FileItemInput,
            fileLength: Long,
            fileUploadId: Long?,
        ): UploadCertificateFormModel {
            val uploadCertificateFormModel =
                when (desiredClass) {
                    GasSafetyUploadCertificateFormModel::class -> GasSafetyUploadCertificateFormModel()
                    EicrUploadCertificateFormModel::class -> EicrUploadCertificateFormModel()
                    else -> throw IllegalStateException("Unsupported desired class: ${desiredClass.simpleName}")
                }

            return uploadCertificateFormModel.apply {
                this.name = fileItemInput.name
                this.contentType = fileItemInput.contentType
                this.contentLength = fileLength
                this.isUploadSuccessfulOrNull = fileUploadId != null
                this.isUserSubmittedMetadataOnly = false
                this.fileUploadId = fileUploadId
            }
        }
    }
}
