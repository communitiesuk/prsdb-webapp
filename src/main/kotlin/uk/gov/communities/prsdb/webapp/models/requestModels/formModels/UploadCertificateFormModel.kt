package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import org.apache.commons.fileupload2.core.FileItemInput
import org.apache.commons.io.FilenameUtils
import kotlin.math.pow
import kotlin.reflect.KClass

abstract class UploadCertificateFormModel : FormModel {
    abstract val certificate: Nothing?

    var name: String = ""

    var contentType: String = ""

    var contentLength: Long = 0

    var isUploadSuccessfulOrNull: Boolean? = null

    var isMetadataOnly: Boolean = true

    fun isNameNotBlank() = name.isNotBlank()

    fun isFileTypeValid() = !isNameNotBlank() || FilenameUtils.getExtension(name) in validExtensions && contentType in validMimeTypes

    fun isContentLengthValid() = !isNameNotBlank() || !isFileTypeValid() || contentLength <= maxContentLength

    fun isUploadSuccessfulOrInvalid() = isUploadSuccessfulOrNull != false

    companion object {
        private val validExtensions = listOf("pdf", "png", "jpeg", "jpg")
        private val validMimeTypes = listOf("application/pdf", "image/png", "image/jpeg")
        val maxContentLength = 15 * 1024.0.pow(2) // 15MB

        fun fromFileItemInput(
            desiredClass: KClass<out UploadCertificateFormModel>,
            fileItemInput: FileItemInput,
            fileLength: Long,
            isUploadSuccessfulOrNull: Boolean? = null,
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
                this.isUploadSuccessfulOrNull = isUploadSuccessfulOrNull
                this.isMetadataOnly = false
            }
        }
    }
}
