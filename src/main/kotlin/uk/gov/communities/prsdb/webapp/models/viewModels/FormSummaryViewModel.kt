package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter

data class FormSummaryViewModel(
    val fieldHeading: String,
    val fieldValue: Any?,
    val changeUrl: String?,
) {
    fun isListSummaryItem(): Boolean = fieldValue is List<*>

    fun getConvertedFieldValue(): Any? =
        if (fieldValue is List<*>) {
            fieldValue.map { getSingleValueMessageKey(it) }
        } else {
            getSingleValueMessageKey(fieldValue)
        }

    private fun getSingleValueMessageKey(value: Any?): Any? =
        when (value) {
            is Enum<*> -> MessageKeyConverter.convert(value)
            is Boolean -> MessageKeyConverter.convert(value)
            else -> value
        }
}