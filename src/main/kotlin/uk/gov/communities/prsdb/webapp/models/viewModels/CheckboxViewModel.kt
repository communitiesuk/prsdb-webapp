package uk.gov.communities.prsdb.webapp.models.viewModels

data class CheckboxViewModel<T>(
    val value: T,
    val valueStr: String = value.toString(),
    val labelMsgKey: String? = null,
)