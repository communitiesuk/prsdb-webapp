package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@PrsdbWebService
class PropertyComplianceViewModelFactory(
    private val gasSafetyViewModelFactory: GasSafetyViewModelFactory,
    private val eicrViewModelFactory: EicrViewModelFactory,
) {
    fun create(
        propertyCompliance: PropertyCompliance,
        landlordView: Boolean = true,
    ): PropertyComplianceViewModel {
        val gasSafetySummaryList: List<SummaryListRowViewModel> =
            gasSafetyViewModelFactory.fromEntity(
                propertyCompliance,
                landlordView,
            )

        val eicrSummaryList: List<SummaryListRowViewModel> =
            eicrViewModelFactory.fromEntity(propertyCompliance, landlordView)

        val epcSummaryList: List<SummaryListRowViewModel> =
            EpcViewModelBuilder.fromEntity(propertyCompliance, landlordView)

        val landlordResponsibilitiesSummaryList: List<SummaryListRowViewModel> =
            LandlordResponsibilitiesViewModelBuilder.fromEntity(
                propertyCompliance,
                landlordView,
            )

        val landlordResponsibilitiesHintText =
            if (landlordView) {
                "propertyDetails.complianceInformation.landlordResponsibilities.landlord.hintText"
            } else {
                "propertyDetails.complianceInformation.landlordResponsibilities.localCouncil.hintText"
            }
        return PropertyComplianceViewModel(
            gasSafetySummaryList = gasSafetySummaryList,
            eicrSummaryList = eicrSummaryList,
            epcSummaryList = epcSummaryList,
            landlordResponsibilitiesSummaryList = landlordResponsibilitiesSummaryList,
            landlordResponsibilitiesHintText = landlordResponsibilitiesHintText,
            notificationMessages = getNotificationMessageKeys(propertyCompliance, landlordView),
        )
    }

    private fun getNotificationMessageKeys(
        propertyCompliance: PropertyCompliance,
        isLandlordView: Boolean,
    ): List<PropertyComplianceViewModel.PropertyComplianceNotificationMessage> =
        mutableListOf<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()
            .apply {
                if (propertyCompliance.isGasSafetyCertExpired == true) {
                    addRow(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                        // TODO PDJB-546: add change link url in here
                        "broken",
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.linkText",
                        "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        withLinkMessage = isLandlordView,
                    )
                }
                if (propertyCompliance.isGasSafetyCertMissing) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                        // TODO PDJB-546: add change link url in here
                        "broken",
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.linkText",
                        "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        withLinkMessage = isLandlordView,
                    )
                }
                if (propertyCompliance.isEicrExpired == true) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                        // TODO PDJB-546: add change link url in here
                        "broken",
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.linkText",
                        "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        withLinkMessage = isLandlordView,
                    )
                }
                if (propertyCompliance.isEicrMissing) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                        // TODO PDJB-546: add change link url in here
                        "broken",
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.linkText",
                        "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        withLinkMessage = isLandlordView,
                    )
                }
                if (propertyCompliance.isEpcExpired == true) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        // TODO PDJB-546: add change link url in here
                        "broken",
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.linkText",
                        "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        withLinkMessage = isLandlordView,
                    )
                }
                if (propertyCompliance.isEpcRatingLow == true) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.mainText",
                        // TODO PDJB-546: add change link url in here
                        "broken",
                        "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.linkText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.afterLinkText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.beforeLinkText",
                        isAfterLinkTextFullStop = true,
                        withLinkMessage = isLandlordView,
                    )
                }
                if (propertyCompliance.isEpcMissing) {
                    addRow(
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                        // TODO PDJB-546: add change link url in here
                        "broken",
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.linkText",
                        "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        withLinkMessage = isLandlordView,
                    )
                }
            }.toList()
}
