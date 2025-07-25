package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

class LandlordDashboardNotificationBannerViewModel(
    val numberOfIncompleteProperties: Int,
    val numberOfIncompleteCompliances: Int,
) {
    val numberOfOutstandingActions: Int = numberOfIncompleteCompliances + numberOfIncompleteProperties

    val showOutstandingActionsHeading: Boolean = numberOfIncompleteProperties != 0 && numberOfIncompleteCompliances != 0

    val onlyIncompleteProperties: Boolean = numberOfIncompleteProperties != 0 && numberOfIncompleteCompliances == 0

    val onlyIncompleteCompliances: Boolean = numberOfIncompleteProperties == 0 && numberOfIncompleteCompliances != 0
}
