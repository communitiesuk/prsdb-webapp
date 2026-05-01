package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel

class PropertyComplianceViewModel(
    val gasSafetySummaryCard: SummaryCardViewModel,
    val electricalSafetySummaryCard: SummaryCardViewModel,
    val epcSummaryCard: SummaryCardViewModel,
    val notificationMessages: List<PropertyComplianceNotificationMessage>,
) {
    data class PropertyComplianceNotificationMessage(
        val mainText: String,
        val linkMessage: PropertyComplianceLinkMessage? = null,
    )

    data class PropertyComplianceLinkMessage(
        val linkUrl: String,
        val linkText: String,
        val afterLinkText: String,
        val beforeLinkText: String? = null,
        val isAfterLinkTextFullStop: Boolean = false,
    )
}
