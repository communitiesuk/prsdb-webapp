package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class PropertyComplianceViewModel(
    private val propertyCompliance: PropertyCompliance,
    private val landlordView: Boolean = true,
) {
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
                        "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        withLinkMessage = landlordView,
                    )
                }
                if (propertyCompliance.isGasSafetyCertMissing) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.linkText",
                        "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        withLinkMessage = landlordView,
                    )
                }
                if (propertyCompliance.isEicrExpired == true) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.linkText",
                        "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        withLinkMessage = landlordView,
                    )
                }
                if (propertyCompliance.isEicrMissing) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.linkText",
                        "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        withLinkMessage = landlordView,
                    )
                }
                if (propertyCompliance.isEpcExpired == true) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.linkText",
                        "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        withLinkMessage = landlordView,
                    )
                }
                if (propertyCompliance.isEpcRatingLow == true) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.linkText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.afterLinkText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.beforeLinkText",
                        withLinkMessage = landlordView,
                    )
                }
                if (propertyCompliance.isEpcMissing) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.linkText",
                        "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        withLinkMessage = landlordView,
                    )
                }
            }.toList()

    data class PropertyComplianceNotificationMessage(
        val mainText: String,
        val linkMessage: PropertyComplianceLinkMessage? = null,
    )

    data class PropertyComplianceLinkMessage(
        val linkText: String,
        val afterLinkText: String,
        val beforeLinkText: String? = null,
    )
}
