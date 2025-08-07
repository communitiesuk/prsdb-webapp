package uk.gov.communities.prsdb.webapp.models.dataModels

data class UploadedFileLocator(
    val objectKey: String,
    val eTag: String,
    val versionId: String?,
)
