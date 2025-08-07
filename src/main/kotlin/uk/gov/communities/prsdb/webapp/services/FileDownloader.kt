package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload

interface FileDownloader {
    // If no fileName is provided, the file will be downloaded with its original name.
    fun getDownloadUrl(
        fileUpload: FileUpload,
        fileName: String? = null,
    ): String?

    fun isFileDownloadable(fileUpload: FileUpload): Boolean = fileUpload.status == FileUploadStatus.SCANNED
}
