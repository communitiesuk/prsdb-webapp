package uk.gov.communities.prsdb.webapp.helpers.extenstions

import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

fun MutableList<SummaryListRowViewModel>.addRow(
    key: String,
    value: Any?,
    changeLink: String? = null,
    withChangeLinks: Boolean = true,
    valueUrl: String? = null,
) {
    val changeLinkOrNull = if (withChangeLinks) changeLink else null
    add(SummaryListRowViewModel(key, value, changeLinkOrNull, valueUrl))
}
