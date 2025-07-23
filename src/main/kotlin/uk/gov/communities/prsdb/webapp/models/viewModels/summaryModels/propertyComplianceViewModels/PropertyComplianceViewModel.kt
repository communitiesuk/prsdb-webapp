package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class PropertyComplianceViewModel(
    private val propertyCompliance: PropertyCompliance,
    private val landlordView: Boolean = true,
) {
    // TODO PRSD-1297 add update links to notification messages
    var notificationMessages: List<PropertyComplianceNotificationMessage> = getNotificationMessageKeys()

    val gasSafetySummaryList: List<SummaryListRowViewModel> =
        GasSafetyViewModelBuilder.fromEntity(
            propertyCompliance,
            landlordView,
        )

    val eicrSummaryList: List<SummaryListRowViewModel> = EicrViewModelBuilder.fromEntity(propertyCompliance, landlordView)

    val epcSummaryList: List<SummaryListRowViewModel> = EpcViewModelBuilder.fromEntity(propertyCompliance, landlordView)

    val landlordResponsibilitiesSummaryList: List<SummaryListRowViewModel> =
        LandlordResponsibilitiesViewModelBuilder.fromEntity(
            propertyCompliance,
            landlordView,
        )

    val landlordResponsibilitiesHintText =
        if (landlordView) {
            "propertyDetails.complianceInformation.landlordResponsibilities.landlord.hintText"
        } else {
            "propertyDetails.complianceInformation.landlordResponsibilities.localAuthority.hintText"
        }

    private fun getNotificationMessageKeys(): List<PropertyComplianceNotificationMessage> =
        mutableListOf<PropertyComplianceNotificationMessage>()
            .apply {
                if (propertyCompliance.isGasSafetyCertExpired == true) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.linkText",
                        landlordView,
                    )
                }
                if (propertyCompliance.isGasSafetyCertMissing) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.linkText",
                        landlordView,
                    )
                }
                if (propertyCompliance.isEicrExpired == true) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.linkText",
                        landlordView,
                    )
                }
                if (propertyCompliance.isEicrMissing) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.linkText",
                        landlordView,
                    )
                }
                if (propertyCompliance.isEpcExpired == true) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.linkText",
                        landlordView,
                    )
                }
                if (propertyCompliance.isEpcRatingLow == true) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.linkText",
                        landlordView,
                    )
                }
                if (propertyCompliance.isEpcMissing) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.linkText",
                        landlordView,
                    )
                }
            }.toList()

    data class PropertyComplianceNotificationMessage(
        val mainText: String,
        val linkText: String? = null,
    )
}
