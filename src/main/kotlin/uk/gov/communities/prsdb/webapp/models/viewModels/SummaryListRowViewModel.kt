package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter

data class SummaryListRowViewModel(
    val fieldHeading: String,
    val fieldValue: Any?,
    val changeUrl: String?,
) {
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
