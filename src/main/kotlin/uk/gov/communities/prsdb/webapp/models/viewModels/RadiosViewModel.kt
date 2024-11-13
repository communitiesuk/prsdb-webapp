package uk.gov.communities.prsdb.webapp.models.viewModels

data class RadiosViewModel<T>(
    val value: T,
    val labelMsgKey: String,
    val hintMsgKey: String? = null,
    val conditionalFragCall: String? = null,
)
