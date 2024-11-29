package uk.gov.communities.prsdb.webapp.models.viewModels

abstract class RadiosViewModel(
    open val labelMsgKey: String? = null,
    val isDivider: Boolean = false,
)

data class RadiosButtonViewModel<T>(
    val value: T,
    val valueStr: String = value.toString(),
    override val labelMsgKey: String? = null,
    val hintMsgKey: String? = null,
    val conditionalFragment: String? = null,
) : RadiosViewModel(labelMsgKey)

data class RadiosDividerViewModel(
    override val labelMsgKey: String?,
) : RadiosViewModel(labelMsgKey, true)
