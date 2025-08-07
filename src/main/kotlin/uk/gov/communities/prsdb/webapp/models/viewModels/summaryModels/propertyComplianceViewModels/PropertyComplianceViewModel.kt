package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class PropertyComplianceViewModel(
    val gasSafetySummaryList: List<SummaryListRowViewModel>,
    val eicrSummaryList: List<SummaryListRowViewModel>,
    val epcSummaryList: List<SummaryListRowViewModel>,
    val landlordResponsibilitiesSummaryList: List<SummaryListRowViewModel>,
    val landlordResponsibilitiesHintText: String,
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
