package uk.gov.communities.prsdb.webapp.helpers.extensions

import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

fun MutableList<SummaryCardViewModel>.addCard(
    title: String,
    cardNumber: String? = null,
    summaryList: List<SummaryListRowViewModel>,
    actions: List<SummaryCardActionViewModel>? = null,
) {
    add(SummaryCardViewModel(title, cardNumber, summaryList, actions))
}

fun MutableList<SummaryCardActionViewModel>.addAction(
    text: String,
    url: String,
) {
    add(SummaryCardActionViewModel(text, url))
}
