package uk.gov.communities.prsdb.webapp.models.dataModels

data class FormSummaryDataModel(
    val fieldHeading: String,
    val fieldValue: Any?,
    val changeUrl: String?,
) {
    fun isListSummaryItem(): Boolean = fieldValue is List<*>
}
