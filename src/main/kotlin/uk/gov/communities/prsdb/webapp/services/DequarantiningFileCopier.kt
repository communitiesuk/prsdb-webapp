package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.models.dataModels.UploadedFileLocator

interface DequarantiningFileCopier {
    fun copyFile(fileUpload: FileUpload): UploadedFileLocator?
}
