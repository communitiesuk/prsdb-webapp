package uk.gov.communities.prsdb.webapp.helpers

import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.fileupload2.core.FileItemInput
import org.apache.commons.fileupload2.core.FileItemInputIterator
import org.apache.commons.io.FilenameUtils
import org.springframework.http.HttpStatus
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.helpers.MaximumLengthInputStream.Companion.withMaxLength
import uk.gov.communities.prsdb.webapp.helpers.extensions.FileItemInputIteratorExtensions.Companion.discardRemainingFields
import uk.gov.communities.prsdb.webapp.helpers.extensions.FileItemInputIteratorExtensions.Companion.getFirstFileField
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService
import uk.gov.communities.prsdb.webapp.services.UploadService

@PrsdbWebService
class CertificateUploadHelper(
    private val fileUploadCookieService: FileUploadCookieService,
    private val uploadService: UploadService,
    private val validator: Validator,
) {
    fun uploadFileAndReturnFormModel(
        uploadFileName: String,
        fileInputIterator: FileItemInputIterator,
        token: String,
        request: HttpServletRequest,
    ): FormData {
        fileUploadCookieService.validateAndUseToken(token)

        val file =
            fileInputIterator.getFirstFileField()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a valid multipart file upload request")

        val fileUploadId =
            if (isFileValid(file, request.contentLengthLong)) {
                uploadFile(uploadFileName, file, request.contentLengthLong)?.id
            } else {
                null
            }

        fileInputIterator.discardRemainingFields()

        return UploadCertificateFormModel
            .fromUploadedFile(
                file,
                request.contentLengthLong,
                fileUploadId,
            ).toPageData()
    }

    private fun isFileValid(
        file: FileItemInput,
        fileLength: Long,
    ): Boolean {
        val fileFormModel = UploadCertificateFormModel.fromUploadedFileMetadata(file, fileLength)
        return !validator.validateObject(fileFormModel).hasErrors()
    }

    private fun uploadFile(
        uploadFileName: String,
        file: FileItemInput,
        fileLength: Long,
    ): FileUpload? =
        uploadService.uploadFile(
            uploadFileName,
            file.inputStream.withMaxLength(fileLength),
            FilenameUtils.getExtension(file.name),
            file.name,
        )

    companion object {
        fun annotateFormDataForMetadataOnlyFileUpload(formData: FormData): FormData =
            // We must ensure that we can distinguish between a metadata-only file upload and a normal file upload when
            // postJourneyData() is used for a file upload endpoint.
            formData + (UploadCertificateFormModel::isUserSubmittedMetadataOnly.name to true)
    }
}
