package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import org.apache.commons.fileupload2.core.FileItemInput
import org.apache.commons.io.FilenameUtils
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy
import kotlin.math.pow

@IsValidPrioritised
open class UploadCertificateFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                // This message is never shown to users, as when this form model fails validation a subclass will be re-validated and used instead
                messageKey = "forms.uploadCertificate.error.missing.placeholder",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isNameNotBlank",
            ),
            ConstraintDescriptor(
                messageKey = "forms.uploadCertificate.error.wrongType",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isFileTypeValid",
            ),
            ConstraintDescriptor(
                messageKey = "forms.uploadCertificate.error.tooBig",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isContentLengthValid",
            ),
            ConstraintDescriptor(
                messageKey = "forms.uploadCertificate.error.unsuccessfulUpload",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isUploadSuccessfulOrInvalid",
            ),
        ],
    )
    open val certificate: Nothing? = null

    var name: String = ""

    var fileUploadId: Long? = null

    var contentType: String = ""

    var contentLength: Long = 0

    var hasUploadFailed: Boolean = false

    var isUserSubmittedMetadataOnly: Boolean = true

    fun isNameNotBlank() = name.isNotBlank()

    fun isFileTypeValid() = !isNameNotBlank() || FilenameUtils.getExtension(name) in validExtensions && contentType in validMimeTypes

    fun isContentLengthValid() = !isNameNotBlank() || !isFileTypeValid() || contentLength <= maxContentLength

    fun isUploadSuccessfulOrInvalid() = !hasUploadFailed

    companion object {
        private val validExtensions = listOf("pdf", "png", "jpeg", "jpg")
        val validMimeTypes = listOf("application/pdf", "image/png", "image/jpeg")
        val maxContentLength = 15 * 1024.0.pow(2) // 15MB

        fun fromUploadedFileMetadata(
            fileItemInput: FileItemInput,
            fileLength: Long,
        ): UploadCertificateFormModel {
            val uploadCertificateFormModel = UploadCertificateFormModel()

            return uploadCertificateFormModel.apply {
                this.name = fileItemInput.name
                this.contentType = fileItemInput.contentType
                this.contentLength = fileLength
                this.hasUploadFailed = false
                this.isUserSubmittedMetadataOnly = false
            }
        }

        fun fromUploadedFile(
            fileItemInput: FileItemInput,
            fileLength: Long,
            fileUploadId: Long?,
        ): UploadCertificateFormModel {
            val uploadCertificateFormModel = UploadCertificateFormModel()

            return uploadCertificateFormModel.apply {
                this.name = fileItemInput.name
                this.contentType = fileItemInput.contentType
                this.contentLength = fileLength
                this.hasUploadFailed = fileUploadId == null
                this.isUserSubmittedMetadataOnly = false
                this.fileUploadId = fileUploadId
            }
        }
    }
}
