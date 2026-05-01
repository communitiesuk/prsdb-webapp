package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

data class UploadedFileUrl(
    val messageKey: String,
    val displayName: String? = null,
    val url: String? = null,
    val urlOpensNewTab: Boolean = false,
)
