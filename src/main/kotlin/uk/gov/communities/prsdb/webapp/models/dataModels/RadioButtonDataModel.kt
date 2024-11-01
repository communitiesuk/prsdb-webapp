package uk.gov.communities.prsdb.webapp.models.dataModels

data class RadioButtonDataModel<T>(
    val value: T,
    val labelMsgKey: String,
    val hintMsgKey: String? = null,
)
