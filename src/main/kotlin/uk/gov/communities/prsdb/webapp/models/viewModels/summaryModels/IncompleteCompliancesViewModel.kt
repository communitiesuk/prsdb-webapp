package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.helpers.extensions.addAction
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompleteComplianceDataModel

class IncompleteCompliancesViewModel(
    private val incompleteCompliancesData: List<IncompleteComplianceDataModel>,
) {
    val incompleteCompliances: List<SummaryCardViewModel> =
        incompleteCompliancesData.mapIndexed { index, compliance ->

            SummaryCardViewModel(
                cardNumber = (index + 1).toString(),
                title = "landlord.incompleteCompliances.summaryCardTitlePrefix",
                summaryList = getSummaryList(compliance),
                actions = getActions(compliance),
            )
        }

    private fun getSummaryList(compliance: IncompleteComplianceDataModel): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "landlord.incompleteCompliances.summaryRow.propertyAddress",
                    compliance.singleLineAddress,
                )
                addRow(
                    "landlord.incompleteCompliances.summaryRow.localAuthority",
                    compliance.localAuthorityName,
                )
                addRow(
                    "landlord.incompleteCompliances.summaryRow.certificatesDue",
                    compliance.certificatesDueDate,
                )
                addRow(
                    "landlord.incompleteCompliances.summaryRow.gasSafety",
                    getComplianceTaskStatus(compliance.gasSafety),
                )
                addRow(
                    "landlord.incompleteCompliances.summaryRow.electricalSafety",
                    getComplianceTaskStatus(compliance.electricalSafety),
                )
                addRow(
                    "landlord.incompleteCompliances.summaryRow.energyPerformance",
                    getComplianceTaskStatus(compliance.energyPerformance),
                )
                addRow(
                    "landlord.incompleteCompliances.SummaryRow.landlordResponsibilities",
                    getComplianceTaskStatus(compliance.landlordsResponsibilities),
                )
            }.toList()

    private fun getActions(compliance: IncompleteComplianceDataModel): List<SummaryCardActionViewModel> =
        mutableListOf<SummaryCardActionViewModel>()
            .apply {
                if (compliance.isComplianceInProgress()) {
                    addAction(
                        "landlord.incompleteCompliances.action.continue",
                        PropertyComplianceController.getPropertyComplianceTaskListPath(compliance.propertyOwnershipId),
                    )
                } else {
                    addAction(
                        "landlord.incompleteCompliances.action.start",
                        PropertyComplianceController.getPropertyCompliancePath(compliance.propertyOwnershipId),
                    )
                }
            }.toList()

    private fun getComplianceTaskStatus(complianceTaskStatus: Boolean): String =
        if (complianceTaskStatus) {
            "landlord.incompleteCompliances.status.added"
        } else {
            "landlord.incompleteCompliances.status.notAdded"
        }
}
