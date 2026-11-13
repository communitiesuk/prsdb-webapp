package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

class InactiveLandlordNotificationBannerViewModel(
    val isOrganisation: Boolean,
    val hasDeactivatedProperties: Boolean,
    val hasIncompleteProperties: Boolean,
) {
    val showReactivationOptionsList: Boolean = hasDeactivatedProperties || hasIncompleteProperties
}
