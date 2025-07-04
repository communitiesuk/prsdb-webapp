package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class PropertyComplianceViewModel(
    private val propertyCompliance: PropertyCompliance,
    private val withActionLinks: Boolean = true,
    private val withNotificationMessages: Boolean = true,
) {
    // TODO PRSD-1297 add update links to notification messages
    var notificationMessages: List<String> = getNotificationMessages()

    val gasSafetySummaryList: List<SummaryListRowViewModel> =
        GasSafetyViewModelBuilder.fromEntity(
            propertyCompliance,
            withActionLinks,
        )

    val landlordResponsibilitiesSummaryList: List<SummaryListRowViewModel> =
        LandlordResponsibilitiesViewModelBuilder.fromEntity(
            propertyCompliance,
            withActionLinks,
        )

    private fun getNotificationMessages(): List<String> {
        if (withNotificationMessages) return emptyList()

        val messageList = mutableListOf<String>()

        if (propertyCompliance.isGasSafetyCertExpired == true) {
            messageList.add("propertyDetails.complianceInformation.notificationMessage.gasCartExpired")
        }
        if (propertyCompliance.isGasSafetyCertMissing) {
            messageList.add("propertyDetails.complianceInformation.notificationMessage.gasCartMissing")
        }

        return messageList
    }
}
