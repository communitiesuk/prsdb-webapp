package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.UpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.controllers.UpdateEpcController
import uk.gov.communities.prsdb.webapp.controllers.UpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.ComplianceActionInsetViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel

private const val VIEW_FULL_EPC_KEY = "propertyCompliance.epcTask.checkEpcAnswers.epc.viewFullEpc"

@PrsdbWebService
class PropertyComplianceViewModelFactory(
    private val gasSafetyViewModelFactory: GasSafetyViewModelFactory,
    private val electricalSafetyViewModelFactory: ElectricalSafetyViewModelFactory,
    private val epcViewModelFactory: EpcViewModelFactory,
) {
    fun create(
        propertyCompliance: PropertyCompliance,
        landlordView: Boolean = true,
        propertyOwnershipId: Long,
    ): PropertyComplianceViewModel {
        val epcChangeActions =
            if (landlordView) {
                SummaryCardActionViewModel.changeAction(
                    UpdateEpcController.getUpdateEpcRouteFirstStep(propertyCompliance.propertyOwnership.id),
                )
            } else {
                null
            }

        val electricalSafetyChangeActions =
            if (landlordView) {
                SummaryCardActionViewModel.changeAction(
                    UpdateElectricalSafetyController.getUpdateElectricalSafetyFirstStepRoute(propertyOwnershipId),
                )
            } else {
                null
            }

        val gasSafetyChangeActions =
            if (landlordView) {
                SummaryCardActionViewModel.changeAction(
                    UpdateGasSafetyController.getUpdateGasSafetyFirstStepRoute(propertyOwnershipId),
                )
            } else {
                null
            }

        val gasSafetyInsetTextKey = gasSafetyViewModelFactory.getInsetTextKey(propertyCompliance)
        val gasSafetySummaryCard =
            SummaryCardViewModel(
                title = "propertyDetails.complianceInformation.gasSafety.heading",
                summaryList = gasSafetyViewModelFactory.fromEntity(propertyCompliance),
                actions = gasSafetyChangeActions,
                insetViewModel = gasSafetyInsetTextKey?.let { ComplianceActionInsetViewModel(messageKey = it) },
            )

        val electricalSafetyInsetTextKey = electricalSafetyViewModelFactory.getInsetTextKey(propertyCompliance)
        val electricalSafetySummaryCard =
            SummaryCardViewModel(
                title = "propertyDetails.complianceInformation.electricalSafety.heading",
                summaryList = electricalSafetyViewModelFactory.fromEntity(propertyCompliance),
                actions = electricalSafetyChangeActions,
                insetViewModel = electricalSafetyInsetTextKey?.let { ComplianceActionInsetViewModel(messageKey = it) },
            )

        val epcCertificateUrl = propertyCompliance.epcUrl

        val epcActions =
            buildList {
                if (epcCertificateUrl != null) {
                    add(SummaryCardActionViewModel(VIEW_FULL_EPC_KEY, epcCertificateUrl, opensInNewTab = true))
                }
                if (epcChangeActions != null) {
                    addAll(epcChangeActions)
                }
            }.ifEmpty { null }

        val epcInsetTextKey = epcViewModelFactory.getInsetTextKey(propertyCompliance)
        val epcSupplementarySections = epcViewModelFactory.getSupplementarySections(propertyCompliance)
        val epcSummaryCard =
            SummaryCardViewModel(
                title = "propertyDetails.complianceInformation.energyPerformance.heading",
                summaryList = epcViewModelFactory.fromEntity(propertyCompliance),
                actions = epcActions,
                insetViewModel = epcInsetTextKey?.let { ComplianceActionInsetViewModel(messageKey = it) },
            )

        val epcExpiredInsetViewModel = epcViewModelFactory.getEpcExpiredInsetViewModel(propertyCompliance)

        val isAllValid = ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance).isAllValid

        return PropertyComplianceViewModel(
            gasSafetySummaryCard = gasSafetySummaryCard,
            electricalSafetySummaryCard = electricalSafetySummaryCard,
            epcSummaryCard = epcSummaryCard,
            epcSupplementarySections = epcSupplementarySections,
            epcExpiredInsetViewModel = epcExpiredInsetViewModel,
            isAllValid = isAllValid,
        )
    }
}
