package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.journeys.Destination

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
) {
    companion object {
        fun changeAction(
            destination: Destination,
            actionText: String = "forms.links.change",
        ): List<SummaryCardActionViewModel> = changeAction(destination.toUrlStringOrNull(), actionText)

        fun changeAction(
            url: String?,
            actionText: String = "forms.links.change",
        ): List<SummaryCardActionViewModel> = url?.let { listOf(SummaryCardActionViewModel(text = actionText, url = it)) } ?: emptyList()
    }
}

data class SummaryCardSupplementarySection(
    val bodyTextKey: String? = null,
    val rows: List<SummaryListRowViewModel> = emptyList(),
)
