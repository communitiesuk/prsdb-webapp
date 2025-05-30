package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

data class SummaryCardViewModel(
    val title: String,
    val cardNumber: String?,
    val summaryList: List<SummaryListRowViewModel>,
    val actions: List<SummaryCardActionViewModel>?,
)

data class SummaryCardActionViewModel(
    val text: String,
    val url: String,
)
