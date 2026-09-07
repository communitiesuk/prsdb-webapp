package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.UpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.controllers.UpdateEpcController
import uk.gov.communities.prsdb.webapp.controllers.UpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.ComplianceActionInsetViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsViewType
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
        viewType: PropertyDetailsViewType = PropertyDetailsViewType.LANDLORD,
        propertyOwnershipId: Long,
    ): PropertyComplianceViewModel {
        val epcChangeActions =
            changeActionsForViewType(
                viewType,
                UpdateEpcController.getUpdateEpcRouteFirstStep(propertyCompliance.propertyOwnership.id),
            )

        val electricalSafetyChangeActions =
            changeActionsForViewType(
                viewType,
                UpdateElectricalSafetyController.getUpdateElectricalSafetyFirstStepRoute(propertyOwnershipId),
            )

        val gasSafetyChangeActions =
            changeActionsForViewType(
                viewType,
                UpdateGasSafetyController.getUpdateGasSafetyFirstStepRoute(propertyOwnershipId),
            )

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

    // Mirrors PropertyDetailsViewModelBase.rowWithViewTypeSpecificChangeLink: a compliance change link is only shown for
    // the view types that can edit compliance. Landlords always can; local councils never can; letting agents can only
    // once their compliance update journeys supply a route (TODO PDJB-1577, PDJB-1578, PDJB-1579), so until then the
    // letting-agent view renders the cards without change links.
    private fun changeActionsForViewType(
        viewType: PropertyDetailsViewType,
        landlordRoute: String,
        lettingAgentRoute: String? = null,
    ): List<SummaryCardActionViewModel>? =
        when (viewType) {
            PropertyDetailsViewType.LANDLORD -> SummaryCardActionViewModel.changeAction(landlordRoute)
            PropertyDetailsViewType.LOCAL_COUNCIL -> null
            PropertyDetailsViewType.LETTING_AGENT -> lettingAgentRoute?.let { SummaryCardActionViewModel.changeAction(it) }
        }
}
