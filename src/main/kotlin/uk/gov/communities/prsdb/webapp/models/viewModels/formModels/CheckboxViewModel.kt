package uk.gov.communities.prsdb.webapp.models.viewModels.formModels

abstract class CheckboxViewModel(
    open val labelMsgKey: String? = null,
    val isDivider: Boolean = false,
)

data class CheckboxButtonViewModel<T>(
    val value: T,
    val valueStr: String = value.toString(),
    override val labelMsgKey: String? = null,
    val conditionalFragment: String? = null,
) : CheckboxViewModel(labelMsgKey)

data class CheckboxDividerViewModel(
    override val labelMsgKey: String?,
) : CheckboxViewModel(labelMsgKey, true)
