package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import org.apache.commons.fileupload2.core.FileItemInput
import org.apache.commons.io.FilenameUtils
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy
import kotlin.math.pow

@IsValidPrioritised
class UploadCertificateFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.uploadCertificate.error.missing",
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
                targetMethod = "isUploadNotUnsuccessful",
            ),
        ],
    )
    val certificate = null

    var name: String = ""

    var contentType: String = ""

    var contentLength: Long = 0

    var isUploadSuccessful: Boolean? = null

    fun isNameNotBlank() = name.isNotBlank()

    fun isFileTypeValid() = !isNameNotBlank() || FilenameUtils.getExtension(name) in validExtensions && contentType in validMimeTypes

    fun isContentLengthValid() = !isNameNotBlank() || !isFileTypeValid() || contentLength <= maxContentLength

    fun isUploadNotUnsuccessful() = isUploadSuccessful != false

    companion object {
        private val validExtensions = listOf("pdf", "png", "jpeg", "jpg")
        private val validMimeTypes = listOf("application/pdf", "image/png", "image/jpeg")
        private val maxContentLength = 15 * 10.0.pow(6) // 15MB

        fun fromFileItemInput(
            fileItemInput: FileItemInput,
            fileLength: Long,
            isUploadSuccessful: Boolean? = null,
        ) = UploadCertificateFormModel().apply {
            this.name = fileItemInput.name
            this.contentType = fileItemInput.contentType
            this.contentLength = fileLength
            this.isUploadSuccessful = isUploadSuccessful
        }
    }
}
