package uk.gov.communities.prsdb.webapp.models.viewModels

data class RadiosViewModel<T>(
    val value: T,
    val valueStr: String = value.toString(),
    val labelMsgKey: String? = null,
    val hintMsgKey: String? = null,
    val conditionalFragment: String? = null,
)
