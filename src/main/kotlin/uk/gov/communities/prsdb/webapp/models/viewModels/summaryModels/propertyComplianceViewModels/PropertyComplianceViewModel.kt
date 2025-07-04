package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class PropertyComplianceViewModel(
    private val propertyCompliance: PropertyCompliance,
    private val withActionLinks: Boolean = true,
    private val withNotificationMessages: Boolean = true,
) {
    // TODO PRSD-1297 add update links to notification messages
    var notifications: List<String> = getNotificationMessageKeys()

    val gasSafetySummaryList: List<SummaryListRowViewModel> =
        GasSafetyViewModelBuilder.fromEntity(
            propertyCompliance,
            withActionLinks,
        )

    val eicrSummaryList: List<SummaryListRowViewModel> = EicrViewModelBuilder.fromEntity(propertyCompliance, withActionLinks)

    val landlordResponsibilitiesSummaryList: List<SummaryListRowViewModel> =
        LandlordResponsibilitiesViewModelBuilder.fromEntity(
            propertyCompliance,
            withActionLinks,
        )

    private fun getNotificationMessageKeys(): List<String> {
        if (withNotificationMessages) return emptyList()

        val messageList = mutableListOf<String>()

        if (propertyCompliance.isGasSafetyCertExpired == true) {
            messageList.add("propertyDetails.complianceInformation.notificationMessage.gasCert.expired")
        }
        if (propertyCompliance.isGasSafetyCertMissing) {
            messageList.add("propertyDetails.complianceInformation.notificationMessage.gasCert.missing")
        }
        if (propertyCompliance.isEicrExpired == true) {
            messageList.add("propertyDetails.complianceInformation.notificationMessage.eicr.expired")
        }
        if (propertyCompliance.isEicrMissing) {
            messageList.add("propertyDetails.complianceInformation.notificationMessage.eicr.missing")
        }

        return messageList
    }
}
