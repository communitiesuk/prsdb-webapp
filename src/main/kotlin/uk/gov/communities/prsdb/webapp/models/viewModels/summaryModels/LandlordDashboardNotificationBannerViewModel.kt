package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

class LandlordDashboardNotificationBannerViewModel(
    val numberOfIncompleteProperties: Int,
    val numberOfComplianceActions: Int,
) {
    val numberOfOutstandingActions: Int = numberOfComplianceActions + numberOfIncompleteProperties

    val showOutstandingActionsHeading: Boolean = numberOfIncompleteProperties != 0 && numberOfComplianceActions != 0

    val onlyIncompleteProperties: Boolean = numberOfIncompleteProperties != 0 && numberOfComplianceActions == 0

    val onlyComplianceActions: Boolean = numberOfIncompleteProperties == 0 && numberOfComplianceActions != 0
}
