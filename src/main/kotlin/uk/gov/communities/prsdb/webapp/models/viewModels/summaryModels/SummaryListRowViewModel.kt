package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.constants.CHECKING_ANSWERS_FOR_PARAMETER_NAME
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

data class SummaryListRowViewModel(
    val fieldHeading: String,
    val fieldValue: Any?,
    val actions: List<SummaryListRowActionsViewModel> = emptyList(),
    val valueUrl: String? = null,
    val valueUrlOpensNewTab: Boolean = false,
    val withoutBottomBorder: Boolean = false,
    val withAriaLabelForAction: String? = null,
    val optionalFieldHeadingParam: Any? = null,
) {
    fun getConvertedFieldValue(): Any? =
        if (fieldValue is List<*>) {
            fieldValue.map { getSingleValueMessageKey(it) }
        } else {
            getSingleValueMessageKey(fieldValue)
        }

    private fun getSingleValueMessageKey(value: Any?): Any? =
        when (value) {
            is RichTextValue -> value
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
            actionValue: String = "forms.links.change",
            valueUrlOpensNewTab: Boolean = false,
            optionalFieldHeadingParam: Any? = null,
        ): SummaryListRowViewModel =
            SummaryListRowViewModel(
                fieldHeading = fieldHeading,
                optionalFieldHeadingParam = optionalFieldHeadingParam,
                fieldValue = fieldValue,
                actions =
                    actionUrl?.let {
                        listOf(
                            SummaryListRowActionsViewModel(
                                actionValue,
                                "$it?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=$it",
                            ),
                        )
                    } ?: emptyList(),
                valueUrl = valueUrl,
                valueUrlOpensNewTab = valueUrlOpensNewTab,
            )

        fun forCheckYourAnswersPage(
            fieldHeading: String,
            fieldValue: Any?,
            destination: Destination,
            valueUrl: String? = null,
            actionValue: String = "forms.links.change",
            valueUrlOpensNewTab: Boolean = false,
            optionalFieldHeadingParam: Any? = null,
        ): SummaryListRowViewModel =
            SummaryListRowViewModel(
                fieldHeading = fieldHeading,
                optionalFieldHeadingParam = optionalFieldHeadingParam,
                fieldValue = fieldValue,
                actions =
                    destination.toUrlStringOrNull()?.let {
                        listOf(
                            SummaryListRowActionsViewModel(
                                actionValue,
                                it,
                            ),
                        )
                    } ?: emptyList(),
                valueUrl = valueUrl,
                valueUrlOpensNewTab = valueUrlOpensNewTab,
            )

        fun forCheckYourAnswersPage(
            fieldHeading: String,
            fieldValue: Any?,
            actions: List<SummaryListRowActionsInputWithDestination>,
            valueUrl: String? = null,
            valueUrlOpensNewTab: Boolean = false,
            optionalFieldHeadingParam: Any? = null,
        ): SummaryListRowViewModel =
            SummaryListRowViewModel(
                fieldHeading = fieldHeading,
                optionalFieldHeadingParam = optionalFieldHeadingParam,
                fieldValue = fieldValue,
                actions =
                    actions.mapNotNull { action ->
                        action.destination.toUrlStringOrNull()?.let { SummaryListRowActionsViewModel(action.text, it) }
                    },
                valueUrl = valueUrl,
                valueUrlOpensNewTab = valueUrlOpensNewTab,
            )
    }
}

data class SummaryListRowActionsInputWithDestination(
    val text: String,
    val destination: Destination,
)

data class SummaryListRowActionsViewModel(
    val text: String,
    val url: String,
)

data class SingleLineFormattableViewModel(
    val listOfValues: List<String>,
    val separator: String? = null,
)
