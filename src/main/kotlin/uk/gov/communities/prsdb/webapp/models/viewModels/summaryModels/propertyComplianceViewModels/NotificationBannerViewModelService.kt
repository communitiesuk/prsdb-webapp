package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_COMPLIANCE_TAB_MAY26_REDESIGN
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

interface NotificationBannerViewModelService {
    @PrsdbFlip(name = PROPERTY_COMPLIANCE_TAB_MAY26_REDESIGN, alterBean = "notificationBannerViewModelServiceRedesign")
    fun getNotificationMessageKeys(
        propertyCompliance: PropertyCompliance,
    ): List<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>

    @PrsdbFlip(name = PROPERTY_COMPLIANCE_TAB_MAY26_REDESIGN, alterBean = "notificationBannerViewModelServiceRedesign")
    fun getIsAllValid(propertyCompliance: PropertyCompliance): Boolean
}
