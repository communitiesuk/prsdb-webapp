package uk.gov.communities.prsdb.webapp.models.viewModels

data class FormSummaryViewModel(
    val fieldHeading: String,
    val fieldValue: Any?,
    val changeUrl: String?,
) {
    fun isListSummaryItem(): Boolean = fieldValue is List<*>
}
