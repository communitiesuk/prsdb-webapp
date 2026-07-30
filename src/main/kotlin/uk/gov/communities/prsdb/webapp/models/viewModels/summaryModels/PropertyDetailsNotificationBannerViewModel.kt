package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_FRAGMENT

class PropertyDetailsNotificationBannerViewModel private constructor(
    val messages: List<NotificationMessage>,
) {
    data class NotificationMessage(
        val mainText: String? = null,
        val links: List<NotificationBannerLink> = emptyList(),
    )

    data class NotificationBannerLink(
        val linkUrl: String,
        val linkText: String,
        val beforeLinkText: String? = null,
        val afterLinkText: String? = null,
    )

    companion object {
        private const val PREFIX = "propertyDetails.propertyRecord.notificationBanner"
        private const val AFTER_LINK_TEXT_KEY = "$PREFIX.afterLinkText"
        private val PROPERTY_URL = "#$PROPERTY_DETAILS_FRAGMENT"
        private val COMPLIANCE_URL = "#$COMPLIANCE_INFO_FRAGMENT"

        fun fromState(
            isLandlordView: Boolean,
            isOccupied: Boolean,
            isLicensingProvideLater: Boolean,
            isTenancyProvideLater: Boolean,
            complianceMessages: List<NotificationMessage>,
        ): PropertyDetailsNotificationBannerViewModel {
            val hasComplianceIssue = complianceMessages.isNotEmpty()
            val hasPropertyProvideLater = isOccupied && (isLicensingProvideLater || isTenancyProvideLater)

            val messages =
                when {
                    hasPropertyProvideLater && hasComplianceIssue -> listOf(propertyAndComplianceCombinedMessage(isLandlordView))
                    hasPropertyProvideLater ->
                        listOf(provideLaterMessage(isLandlordView, isLicensingProvideLater, isTenancyProvideLater))
                    else -> complianceMessages
                }

            return PropertyDetailsNotificationBannerViewModel(messages)
        }

        private fun viewSegment(isLandlordView: Boolean) = if (isLandlordView) "landlord" else "localCouncil"

        private fun propertyAndComplianceCombinedMessage(isLandlordView: Boolean): NotificationMessage {
            val prefix = "$PREFIX.${viewSegment(isLandlordView)}.propertyAndCompliance"
            return NotificationMessage(
                links =
                    listOf(
                        NotificationBannerLink(
                            linkUrl = PROPERTY_URL,
                            linkText = "$prefix.propertyLinkText",
                            beforeLinkText = "$prefix.beforeLinkText",
                            afterLinkText = "$prefix.middleText",
                        ),
                        NotificationBannerLink(
                            linkUrl = COMPLIANCE_URL,
                            linkText = "$prefix.complianceLinkText",
                            afterLinkText = AFTER_LINK_TEXT_KEY,
                        ),
                    ),
            )
        }

        private fun provideLaterMessage(
            isLandlordView: Boolean,
            isLicensingProvideLater: Boolean,
            isTenancyProvideLater: Boolean,
        ): NotificationMessage {
            val view = viewSegment(isLandlordView)
            return when {
                isLicensingProvideLater && isTenancyProvideLater && isLandlordView -> landlordLicensingAndTenancyMessage()
                isLicensingProvideLater && isTenancyProvideLater -> singleLinkMessage("$PREFIX.$view.licensingAndTenancy")
                isLicensingProvideLater -> singleLinkMessage("$PREFIX.$view.licensing")
                else -> singleLinkMessage("$PREFIX.$view.tenancy")
            }
        }

        private fun landlordLicensingAndTenancyMessage(): NotificationMessage {
            val prefix = "$PREFIX.landlord.licensingAndTenancy"
            return NotificationMessage(
                links =
                    listOf(
                        NotificationBannerLink(
                            linkUrl = PROPERTY_URL,
                            linkText = "$prefix.licensingLinkText",
                            beforeLinkText = "$prefix.beforeLinkText",
                            afterLinkText = "$prefix.middleText",
                        ),
                        NotificationBannerLink(
                            linkUrl = PROPERTY_URL,
                            linkText = "$prefix.tenancyLinkText",
                            afterLinkText = AFTER_LINK_TEXT_KEY,
                        ),
                    ),
            )
        }

        private fun singleLinkMessage(prefix: String) =
            NotificationMessage(
                mainText = "$prefix.mainText",
                links =
                    listOf(
                        NotificationBannerLink(
                            linkUrl = PROPERTY_URL,
                            linkText = "$prefix.linkText",
                            afterLinkText = AFTER_LINK_TEXT_KEY,
                        ),
                    ),
            )
    }
}
