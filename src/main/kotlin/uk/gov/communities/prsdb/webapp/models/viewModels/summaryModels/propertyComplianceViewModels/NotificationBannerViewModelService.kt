package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_ACTIONS_MAY2026_REDESIGN
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

interface NotificationBannerViewModelService {
    @PrsdbFlip(name = COMPLIANCE_ACTIONS_MAY2026_REDESIGN, alterBean = "notificationBannerViewModelServiceRedesign")
    fun getNotificationMessageKeys(
        propertyCompliance: PropertyCompliance,
    ): List<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>

    @PrsdbFlip(name = COMPLIANCE_ACTIONS_MAY2026_REDESIGN, alterBean = "notificationBannerViewModelServiceRedesign")
    fun getIsAllValid(propertyCompliance: PropertyCompliance): Boolean
}
