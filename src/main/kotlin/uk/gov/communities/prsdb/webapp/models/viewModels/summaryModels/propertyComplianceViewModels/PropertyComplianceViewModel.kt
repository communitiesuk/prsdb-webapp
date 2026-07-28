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
    val complianceNotificationMessages: List<NotificationMessage>,
    // Flag-off equivalent of complianceNotificationMessages, using the beforePdjb939 message keys where the copy differs.
    val beforePdjb939ComplianceNotificationMessages: List<NotificationMessage> = emptyList(),
    val isAllValid: Boolean,
)
