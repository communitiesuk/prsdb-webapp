package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel.NotificationMessage

// Compliance-only notification banner for the flag-off path. Reuses [NotificationMessage] so the flag-off template
// renders it identically to the flag-on [PropertyDetailsNotificationBannerViewModel]; unlike that, it never merges
// provide-later messages and uses the beforePdjb939 message keys where the copy differs.
class PropertyDetailsBeforePdjb939NotificationBannerViewModel private constructor(
    val messages: List<NotificationMessage>,
) {
    companion object {
        fun fromState(
            isLandlordView: Boolean,
            complianceMessages: List<NotificationMessage>?,
        ): PropertyDetailsBeforePdjb939NotificationBannerViewModel {
            val messages =
                complianceMessages
                    ?: listOf(NotificationMessage(mainText = noComplianceMessageKey(isLandlordView)))
            return PropertyDetailsBeforePdjb939NotificationBannerViewModel(messages)
        }

        private fun noComplianceMessageKey(isLandlordView: Boolean): String =
            if (isLandlordView) {
                "propertyDetails.complianceInformation.noCompliance.landlordView.mainText"
            } else {
                "propertyDetails.complianceInformation.noCompliance.localCouncilView.mainText"
            }
    }
}
