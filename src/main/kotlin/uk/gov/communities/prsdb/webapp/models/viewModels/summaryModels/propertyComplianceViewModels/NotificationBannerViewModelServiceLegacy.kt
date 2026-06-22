package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

@PrsdbWebService
@Primary
class NotificationBannerViewModelServiceLegacy : NotificationBannerViewModelService {
    override fun getNotificationMessageKeys(
        propertyCompliance: PropertyCompliance,
    ): List<PropertyComplianceViewModel.PropertyComplianceNotificationMessage> = emptyList()

    override fun getIsAllValid(propertyCompliance: PropertyCompliance): Boolean = false
}
