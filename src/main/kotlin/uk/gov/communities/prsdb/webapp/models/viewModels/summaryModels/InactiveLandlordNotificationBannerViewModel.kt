package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

class InactiveLandlordNotificationBannerViewModel(
    val isOrganisation: Boolean,
    val hasDeactivatedProperties: Boolean,
    val hasIncompleteProperties: Boolean,
) {
    val showReactivationOptionsList: Boolean = hasDeactivatedProperties || hasIncompleteProperties

    private val messageKeyPrefix =
        if (isOrganisation) {
            "landlord.dashboard.inactiveLandlordBanner.organisation"
        } else {
            "landlord.dashboard.inactiveLandlordBanner.individual"
        }

    val headingKey: String = "$messageKeyPrefix.heading"

    val beforeLinkKey: String = "$messageKeyPrefix.beforeLink"
}
