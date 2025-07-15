package uk.gov.communities.prsdb.webapp.helpers.extensions

import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

fun MutableList<SummaryListRowViewModel>.addRow(
    key: String,
    value: Any?,
    actionText: String? = null,
    actionLink: String? = null,
    withActionLink: Boolean = true,
    valueUrl: String? = null,
    valueUrlOpensNewTab: Boolean = false,
) {
    val actionOrNull = if (withActionLink) getActionOrNull(actionText, actionLink) else null
    add(SummaryListRowViewModel(key, value, actionOrNull, valueUrl, valueUrlOpensNewTab))
}

fun MutableList<SummaryListRowViewModel>.addRow(
    key: String,
    value: Any?,
    valueUrl: String? = null,
) {
    addRow(key = key, value = value, withActionLink = false, valueUrl = valueUrl)
}

private fun getActionOrNull(
    actionText: String?,
    actionLink: String?,
): SummaryListRowActionViewModel? =
    if (actionText != null && actionLink != null) {
        SummaryListRowActionViewModel(actionText, actionLink)
    } else {
        null
    }
