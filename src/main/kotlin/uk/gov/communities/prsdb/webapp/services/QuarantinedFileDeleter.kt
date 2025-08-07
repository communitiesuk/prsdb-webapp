package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.database.entity.FileUpload

interface QuarantinedFileDeleter {
    fun deleteFile(fileUpload: FileUpload): Boolean
}
