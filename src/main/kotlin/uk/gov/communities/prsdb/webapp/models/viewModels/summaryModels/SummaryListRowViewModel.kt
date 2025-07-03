package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.constants.CHANGE_ANSWER_FOR_PARAMETER_NAME
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

data class SummaryListRowViewModel(
    val fieldHeading: String,
    val fieldValue: Any?,
    val action: SummaryListRowActionViewModel? = null,
    val valueUrl: String? = null,
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
            is RegistrationNumberDataModel -> value.toString()
            is LocalDate -> value.toJavaLocalDate()
            else -> value
        }

    companion object {
        fun forCheckYourAnswersPage(
            fieldHeading: String,
            fieldValue: Any?,
            actionUrl: String?,
            valueUrl: String? = null,
        ): SummaryListRowViewModel =
            SummaryListRowViewModel(
                fieldHeading = fieldHeading,
                fieldValue = fieldValue,
                action =
                    actionUrl?.let {
                        SummaryListRowActionViewModel(
                            "forms.links.change",
                            "$it?$CHANGE_ANSWER_FOR_PARAMETER_NAME=$it",
                        )
                    },
                valueUrl = valueUrl,
            )
    }
}

data class SummaryListRowActionViewModel(
    val text: String,
    val url: String,
)
