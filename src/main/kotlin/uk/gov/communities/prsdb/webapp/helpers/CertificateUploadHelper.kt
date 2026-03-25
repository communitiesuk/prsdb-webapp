package uk.gov.communities.prsdb.webapp.helpers

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.fileupload2.core.FileItemInput
import org.apache.commons.fileupload2.core.FileItemInputIterator
import org.apache.commons.io.FilenameUtils
import org.springframework.http.HttpStatus
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.FILE_UPLOAD_URL_SUBSTRING
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.helpers.MaximumLengthInputStream.Companion.withMaxLength
import uk.gov.communities.prsdb.webapp.helpers.extensions.FileItemInputIteratorExtensions.Companion.discardRemainingFields
import uk.gov.communities.prsdb.webapp.helpers.extensions.FileItemInputIteratorExtensions.Companion.getFirstFileField
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.services.TokenCookieService
import uk.gov.communities.prsdb.webapp.services.UploadService

@PrsdbWebService
class CertificateUploadHelper(
    private val tokenCookieService: TokenCookieService,
    private val uploadService: UploadService,
    private val validator: Validator,
) {
    fun uploadFileAndReturnFormModel(
        uploadFileName: String,
        fileInputIterator: FileItemInputIterator,
        token: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
        cookieName: String,
    ): PageData {
        if (tokenCookieService.isTokenForCookieValue(token, request.requestURI)) {
            tokenCookieService.useToken(token)
        } else {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid upload token")
        }

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

        if (fileUploadId == null) {
            val cookie = tokenCookieService.createCookieForValue(cookieName, request.requestURI)
            response.addCookie(cookie)
        }

        return UploadCertificateFormModel
            .fromUploadedFile(
                file,
                request.contentLengthLong,
                fileUploadId,
            ).toPageData()
    }

    fun addCookieIfStepIsFileUploadStep(
        stepName: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
        cookieName: String,
    ) {
        if (stepName.contains(FILE_UPLOAD_URL_SUBSTRING)) {
            val cookie = tokenCookieService.createCookieForValue(cookieName, request.requestURI)
            response.addCookie(cookie)
        }
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
        )

    companion object {
        fun annotateFormDataForMetadataOnlyFileUpload(formData: PageData): PageData =
            // We must ensure that we can distinguish between a metadata-only file upload and a normal file upload when
            // postJourneyData() is used for a file upload endpoint.
            formData + (UploadCertificateFormModel::isUserSubmittedMetadataOnly.name to true)
    }
}
