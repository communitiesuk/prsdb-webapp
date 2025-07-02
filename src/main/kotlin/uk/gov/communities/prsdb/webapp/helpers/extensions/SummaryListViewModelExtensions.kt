package uk.gov.communities.prsdb.webapp.helpers.extensions

import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

fun MutableList<SummaryListRowViewModel>.addRow(
    key: String,
    value: Any?,
    actionText: String? = null,
    actionLink: String? = null,
    withActionLinks: Boolean = true,
    valueUrl: String? = null,
) {
    val actionOrNull = if (withActionLinks) getActionOrNull(actionText, actionLink) else null
    add(SummaryListRowViewModel(key, value, actionOrNull, valueUrl))
}

fun MutableList<SummaryListRowViewModel>.addRow(
    key: String,
    value: Any?,
    valueUrl: String? = null,
) {
    addRow(key = key, value = value, withActionLinks = false, valueUrl = valueUrl)
}

private fun getActionOrNull(
    actionText: String?,
    actionLink: String?,
): SummaryListActionViewModel? = actionText?.let { actionLink?.let { SummaryListActionViewModel(actionText, it) } }
