package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

data class SummaryCardViewModel(
    val title: String,
    val summaryList: List<SummaryListRowViewModel>,
    val actions: List<SummaryCardActionViewModel>? = null,
    val cardNumber: String? = null,
    val insetViewModel: ComplianceActionInsetViewModel? = null,
)

data class SummaryCardActionViewModel(
    val text: String,
    val url: String,
    val opensInNewTab: Boolean = false,
)

data class SummaryCardSupplementarySection(
    val bodyTextKey: String? = null,
    val rows: List<SummaryListRowViewModel> = emptyList(),
)
