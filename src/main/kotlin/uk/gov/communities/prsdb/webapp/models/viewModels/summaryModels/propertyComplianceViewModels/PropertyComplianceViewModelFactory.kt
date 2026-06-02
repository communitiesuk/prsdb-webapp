package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.UpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.controllers.UpdateEpcController
import uk.gov.communities.prsdb.webapp.controllers.UpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.ComplianceActionInsetViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel

@PrsdbWebService
class PropertyComplianceViewModelFactory(
    private val gasSafetyViewModelService: GasSafetyViewModelService,
    private val electricalSafetyViewModelService: ElectricalSafetyViewModelService,
    private val notificationBannerViewModelService: NotificationBannerViewModelService,
) {
    fun create(
        propertyCompliance: PropertyCompliance,
        landlordView: Boolean = true,
        propertyOwnershipId: Long,
    ): PropertyComplianceViewModel {
        val epcChangeActions =
            if (landlordView) {
                listOf(
                    SummaryCardActionViewModel(
                        "forms.links.change",
                        UpdateEpcController.getUpdateEpcRouteFirstStep(propertyCompliance.propertyOwnership.id),
                    ),
                )
            } else {
                null
            }

        val electricalSafetyChangeActions =
            if (landlordView) {
                listOf(
                    SummaryCardActionViewModel(
                        "forms.links.change",
                        UpdateElectricalSafetyController.getUpdateElectricalSafetyFirstStepRoute(propertyOwnershipId),
                    ),
                )
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

        val gasSafetyInsetTextKey = gasSafetyViewModelService.getInsetTextKey(propertyCompliance)
        val gasSafetySummaryCard =
            SummaryCardViewModel(
                title = "propertyDetails.complianceInformation.gasSafety.heading",
                summaryList = gasSafetyViewModelService.fromEntity(propertyCompliance),
                actions = gasSafetyChangeActions,
                insetViewModel = gasSafetyInsetTextKey?.let { ComplianceActionInsetViewModel(messageKey = it) },
            )

        val electricalSafetyInsetTextKey = electricalSafetyViewModelService.getInsetTextKey(propertyCompliance)
        val electricalSafetySummaryCard =
            SummaryCardViewModel(
                title = "propertyDetails.complianceInformation.electricalSafety.heading",
                summaryList = electricalSafetyViewModelService.fromEntity(propertyCompliance),
                actions = electricalSafetyChangeActions,
                insetViewModel = electricalSafetyInsetTextKey?.let { ComplianceActionInsetViewModel(messageKey = it) },
            )

        val epcSummaryCard =
            SummaryCardViewModel(
                title = "propertyDetails.complianceInformation.energyPerformance.heading",
                summaryList = EpcViewModelBuilder.fromEntity(propertyCompliance),
                actions = epcChangeActions,
            )

        val notificationMessages = notificationBannerViewModelService.getNotificationMessageKeys(propertyCompliance)

        val isAllValid = notificationBannerViewModelService.getIsAllValid(propertyCompliance)

        return PropertyComplianceViewModel(
            gasSafetySummaryCard = gasSafetySummaryCard,
            electricalSafetySummaryCard = electricalSafetySummaryCard,
            epcSummaryCard = epcSummaryCard,
            notificationMessages = notificationMessages,
            isAllValid = isAllValid,
        )
    }
}
