package uk.gov.communities.prsdb.webapp.helpers.extensions

import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowActionsViewModel
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
    val actionsList = if (withActionLink) getActionsOrEmptyList(actionText, actionLink) else emptyList()
    add(SummaryListRowViewModel(key, value, actionsList, valueUrl, valueUrlOpensNewTab))
}

fun MutableList<SummaryListRowViewModel>.addRow(
    key: String,
    value: Any?,
    valueUrl: String? = null,
) {
    addRow(key = key, value = value, withActionLink = false, valueUrl = valueUrl)
}

private fun getActionsOrEmptyList(
    actionText: String?,
    actionLink: String?,
): List<SummaryListRowActionsViewModel> =
    if (actionText != null && actionLink != null) {
        listOf(SummaryListRowActionsViewModel(actionText, actionLink))
    } else {
        emptyList()
    }
