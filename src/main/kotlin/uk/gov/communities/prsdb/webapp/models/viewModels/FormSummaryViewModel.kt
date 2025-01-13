package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter

data class FormSummaryViewModel(
    val fieldHeading: String,
    val summaryRow: UnlabelledSummaryListRowViewModel,
) : SummaryListRowViewModel {
    constructor(
        fieldHeading: String,
        fieldValue: Any?,
        changeUrl: String?,
    ) : this(fieldHeading, UnlabelledSummaryListRowViewModel(fieldValue, changeUrl))

    override val fieldValue: Any?
        get() = summaryRow.fieldValue

    override val changeUrl: String?
        get() = summaryRow.changeUrl

    override fun getConvertedFieldValue(): Any? = summaryRow.getConvertedFieldValue()
}

data class UnlabelledSummaryListRowViewModel(
    override val fieldValue: Any?,
    override val changeUrl: String?,
) : SummaryListRowViewModel {
    override fun getConvertedFieldValue(): Any? =
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

interface SummaryListRowViewModel {
    val fieldValue: Any?
    val changeUrl: String?

    fun getConvertedFieldValue(): Any?
}
