package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.EpcExpiredInsetViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel

class PropertyComplianceViewModel(
    val gasSafetySummaryCard: SummaryCardViewModel,
    val electricalSafetySummaryCard: SummaryCardViewModel,
    val epcSummaryCard: SummaryCardViewModel,
    val epcExpiredInsetViewModel: EpcExpiredInsetViewModel? = null,
    val notificationMessages: List<PropertyComplianceNotificationMessage>,
    val isAllValid: Boolean,
) {
    data class PropertyComplianceNotificationMessage(
        val mainText: String,
        val linkMessage: PropertyComplianceLinkMessage? = null,
    )

    data class PropertyComplianceLinkMessage(
        val linkUrl: String,
        val linkText: String,
        val afterLinkText: String? = null,
        val beforeLinkText: String? = null,
        val isAfterLinkTextFullStop: Boolean = false,
    )
}
