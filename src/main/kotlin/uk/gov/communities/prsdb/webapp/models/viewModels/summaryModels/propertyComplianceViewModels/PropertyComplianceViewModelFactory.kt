package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.UpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel

@PrsdbWebService
class PropertyComplianceViewModelFactory(
    private val gasSafetyViewModelFactory: GasSafetyViewModelFactory,
    private val electricalSafetyViewModelFactory: ElectricalSafetyViewModelFactory,
) {
    fun create(
        propertyCompliance: PropertyCompliance,
        landlordView: Boolean = true,
        propertyOwnershipId: Long,
    ): PropertyComplianceViewModel {
        val changeActions =
            if (landlordView) {
                listOf(
                    SummaryCardActionViewModel("forms.links.change", "#"),
                ) // TODO PDJB-765, PDJB-766: replace with actual journey URLs
            } else {
                null
            }

        val gasSafetyChangeActions =
            if (landlordView) {
                listOf(
                    SummaryCardActionViewModel(
                        "forms.links.change",
                        UpdateGasSafetyController.getUpdateGasSafetyFirstStepRoute(propertyOwnershipId),
                    ),
                )
            } else {
                null
            }

        val gasSafetySummaryCard =
            SummaryCardViewModel(
                title = "propertyDetails.complianceInformation.gasSafety.heading",
                summaryList = gasSafetyViewModelFactory.fromEntity(propertyCompliance),
                actions = gasSafetyChangeActions,
            )

        val electricalSafetySummaryCard =
            SummaryCardViewModel(
                title = "propertyDetails.complianceInformation.electricalSafety.heading",
                summaryList = electricalSafetyViewModelFactory.fromEntity(propertyCompliance),
                actions = changeActions,
            )

        val epcSummaryCard =
            SummaryCardViewModel(
                title = "propertyDetails.complianceInformation.energyPerformance.heading",
                summaryList = EpcViewModelBuilder.fromEntity(propertyCompliance),
                actions = changeActions,
            )

        return PropertyComplianceViewModel(
            gasSafetySummaryCard = gasSafetySummaryCard,
            electricalSafetySummaryCard = electricalSafetySummaryCard,
            epcSummaryCard = epcSummaryCard,
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
