package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@PrsdbWebService
class PropertyComplianceViewModelFactory(
    private val gasSafetyViewModelFactory: GasSafetyViewModelFactory,
    private val eicrViewModelFactory: EicrViewModelFactory,
) {
    fun create(
        propertyCompliance: PropertyCompliance,
        landlordView: Boolean = true,
        propertyOwnershipId: Long,
    ): PropertyComplianceViewModel {
        val gasSafetySummaryList: List<SummaryListRowViewModel> =
            gasSafetyViewModelFactory.fromEntity(
                propertyCompliance,
                landlordView,
                propertyOwnershipId,
            )

        val eicrSummaryList: List<SummaryListRowViewModel> =
            eicrViewModelFactory.fromEntity(propertyCompliance, landlordView, propertyOwnershipId)

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
                // TODO: PDJB-794: reinstate notifications for gas safety cert missing/expired, eicr missing/expired and epc missing/expired/low rating
                emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()
            }
}
