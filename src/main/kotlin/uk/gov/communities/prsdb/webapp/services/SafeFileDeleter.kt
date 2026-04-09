package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.database.entity.FileUpload

interface SafeFileDeleter {
    fun deleteFile(fileUpload: FileUpload): Boolean
}
