package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class PropertyComplianceViewModel(
    private val propertyCompliance: PropertyCompliance,
    private val withActionLinks: Boolean = true,
    private val withNotificationLinks: Boolean = true,
) {
    // TODO PRSD-1297 add update links to notification messages
    var notificationMessages: List<PropertyComplianceNotificationMessage> = getNotificationMessageKeys()

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

    private fun getNotificationMessageKeys(): List<PropertyComplianceNotificationMessage> =
        mutableListOf<PropertyComplianceNotificationMessage>()
            .apply {
                if (propertyCompliance.isGasSafetyCertExpired == true) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.linkText",
                        withNotificationLinks,
                    )
                }
                if (propertyCompliance.isGasSafetyCertMissing) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.linkText",
                        withNotificationLinks,
                    )
                }
                if (propertyCompliance.isEicrExpired == true) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.linkText",
                        withNotificationLinks,
                    )
                }
                if (propertyCompliance.isEicrMissing) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.linkText",
                        withNotificationLinks,
                    )
                }
                if (propertyCompliance.isEpcExpired == true) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.linkText",
                        withNotificationLinks,
                    )
                }
                if (propertyCompliance.isEpcRatingLow == true) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.linkText",
                        withNotificationLinks,
                    )
                }
                if (propertyCompliance.isEpcMissing) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.linkText",
                        withNotificationLinks,
                    )
                }
            }.toList()

    data class PropertyComplianceNotificationMessage(
        val mainText: String,
        val linkText: String? = null,
    )
}
