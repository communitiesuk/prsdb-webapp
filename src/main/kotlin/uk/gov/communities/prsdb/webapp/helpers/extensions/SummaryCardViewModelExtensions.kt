package uk.gov.communities.prsdb.webapp.helpers.extensions

import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel

fun MutableList<SummaryCardActionViewModel>.addAction(
    text: String,
    url: String,
) {
    add(SummaryCardActionViewModel(text, url))
}
