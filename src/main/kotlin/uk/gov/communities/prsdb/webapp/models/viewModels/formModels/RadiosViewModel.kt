package uk.gov.communities.prsdb.webapp.models.viewModels.formModels

abstract class RadiosViewModel(
    open val labelMsgKey: String? = null,
    val isDivider: Boolean = false,
) {
    companion object {
        fun yesOrNoRadios(
            yesHint: String? = null,
            noHint: String? = null,
            noLabel: String = "forms.radios.option.no.label",
        ) = listOf(
            RadiosButtonViewModel(
                value = true,
                valueStr = "yes",
                labelMsgKey = "forms.radios.option.yes.label",
                hintMsgKey = yesHint,
            ),
            RadiosButtonViewModel(
                value = false,
                valueStr = "no",
                labelMsgKey = noLabel,
                hintMsgKey = noHint,
            ),
        )
    }
}

data class RadiosButtonViewModel<T>(
    val value: T,
    val valueStr: String = value.toString(),
    override val labelMsgKey: String? = null,
    val hintMsgKey: String? = null,
    val hintMsgArg: Any? = null,
    val hintValue: String? = null,
    val conditionalFragment: String? = null,
) : RadiosViewModel(labelMsgKey)

data class RadiosDividerViewModel(
    override val labelMsgKey: String?,
) : RadiosViewModel(labelMsgKey, true)
