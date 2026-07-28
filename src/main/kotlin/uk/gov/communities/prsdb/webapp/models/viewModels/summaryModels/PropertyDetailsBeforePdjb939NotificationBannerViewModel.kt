package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel.NotificationMessage

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
