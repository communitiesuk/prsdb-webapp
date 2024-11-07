package uk.gov.communities.prsdb.webapp.models.formModels

data class RadioButtonDataModel<T>(
    val value: T,
    val labelMsgKey: String,
    val hintMsgKey: String? = null,
)
