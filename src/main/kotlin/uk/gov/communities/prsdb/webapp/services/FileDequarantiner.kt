package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.database.entity.FileUpload

interface FileDequarantiner {
    fun dequarantineFile(fileUpload: FileUpload): Boolean

    fun deleteFile(fileUpload: FileUpload): Boolean
}
