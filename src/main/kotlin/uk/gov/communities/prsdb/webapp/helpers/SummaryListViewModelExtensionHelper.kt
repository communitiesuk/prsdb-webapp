package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.models.viewModels.SummaryListRowViewModel

class SummaryListViewModelExtensionHelper {
    fun MutableList<SummaryListRowViewModel>.addRow(
        key: String,
        value: Any?,
        changeLink: String? = null,
        valueUrl: String? = null,
        withChangeLinks: Boolean = true,
    ) {
        val changeLinkOrNull = if (withChangeLinks) changeLink else null
        add(SummaryListRowViewModel(key, value, changeLinkOrNull, valueUrl))
    }
}
