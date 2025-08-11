package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

data class SummaryCardViewModel(
    val title: String,
    val summaryList: List<SummaryListRowViewModel>,
    val actions: List<SummaryCardActionViewModel>?,
    val cardNumber: String? = null,
)

data class SummaryCardActionViewModel(
    val text: String,
    val url: String,
)
