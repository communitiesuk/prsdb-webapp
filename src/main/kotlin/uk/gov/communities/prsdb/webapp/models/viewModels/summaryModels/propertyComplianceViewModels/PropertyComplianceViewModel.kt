package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.EpcExpiredInsetViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel.NotificationMessage
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardSupplementarySection
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel

class PropertyComplianceViewModel(
    val gasSafetySummaryCard: SummaryCardViewModel,
    val electricalSafetySummaryCard: SummaryCardViewModel,
    val epcSummaryCard: SummaryCardViewModel,
    val epcSupplementarySections: List<SummaryCardSupplementarySection> = emptyList(),
    val epcExpiredInsetViewModel: EpcExpiredInsetViewModel? = null,
    val notificationMessages: List<NotificationMessage>,
    // TODO PDJB-939: remove beforePdjb939NotificationMessages and the two nested classes below when the
    //  provide-later flag is permanently on; the flag-on banner uses notificationMessages above.
    val beforePdjb939NotificationMessages: List<PropertyComplianceNotificationMessage> = emptyList(),
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
