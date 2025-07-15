package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class PropertyComplianceViewModel(
    private val propertyCompliance: PropertyCompliance,
    private val withActionLinks: Boolean = true,
    private val withNotificationMessages: Boolean = true,
) {
    // TODO PRSD-1297 add update links to notification messages
    var notificationMessages: List<String> = if (withNotificationMessages) getNotificationMessageKeys() else emptyList()

    val gasSafetySummaryList: List<SummaryListRowViewModel> =
        GasSafetyViewModelBuilder.fromEntity(
            propertyCompliance,
            withActionLinks,
        )

    val eicrSummaryList: List<SummaryListRowViewModel> = EicrViewModelBuilder.fromEntity(propertyCompliance, withActionLinks)

    val epcSummaryList: List<SummaryListRowViewModel> = EpcViewModelBuilder.fromEntity(propertyCompliance, withActionLinks)

    val landlordResponsibilitiesSummaryList: List<SummaryListRowViewModel> =
        LandlordResponsibilitiesViewModelBuilder.fromEntity(
            propertyCompliance,
            withActionLinks,
        )

    private fun getNotificationMessageKeys(): List<String> =
        mutableListOf<String>()
            .apply {
                if (propertyCompliance.isGasSafetyCertExpired == true) {
                    add("propertyDetails.complianceInformation.notificationBanner.gasCert.expired")
                }
                if (propertyCompliance.isGasSafetyCertMissing) {
                    add("propertyDetails.complianceInformation.notificationBanner.gasCert.missing")
                }
                if (propertyCompliance.isEicrExpired == true) {
                    add("propertyDetails.complianceInformation.notificationBanner.eicr.expired")
                }
                if (propertyCompliance.isEicrMissing) {
                    add("propertyDetails.complianceInformation.notificationBanner.eicr.missing")
                }
                if (propertyCompliance.isEpcExpired == true) {
                    add("propertyDetails.complianceInformation.notificationBanner.epc.expired")
                }
                if (propertyCompliance.isEpcRatingLow == true) {
                    add("propertyDetails.complianceInformation.notificationBanner.epc.lowRating")
                }
                if (propertyCompliance.isEpcMissing) {
                    add("propertyDetails.complianceInformation.notificationBanner.epc.missing")
                }
            }.toList()
}
