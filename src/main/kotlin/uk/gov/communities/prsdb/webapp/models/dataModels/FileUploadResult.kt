package uk.gov.communities.prsdb.webapp.models.dataModels

data class FileUploadResult(
    val objectKey: String,
    val eTag: String,
    val versionId: String?,
)
