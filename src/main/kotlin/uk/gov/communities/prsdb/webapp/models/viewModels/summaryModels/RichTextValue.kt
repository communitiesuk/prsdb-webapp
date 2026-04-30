package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

data class RichTextValue(
    val textKey: String,
    val url: String? = null,
    val urlOpensNewTab: Boolean = false,
    val optionalParam: Any? = null,
)
