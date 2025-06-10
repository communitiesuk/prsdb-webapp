package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.helpers.extensions.addAction
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompleteComplianceDataModel

class IncompleteComplianceViewModelBuilder {
    companion object {
        fun fromDataModel(
            index: Int,
            dataModel: IncompleteComplianceDataModel,
        ): SummaryCardViewModel =
            SummaryCardViewModel(
                cardNumber = (index + 1).toString(),
                title = "landlord.incompleteCompliances.summaryCardTitlePrefix",
                summaryList = getSummaryList(dataModel),
                actions = getActions(dataModel),
            )

        private fun getSummaryList(dataModel: IncompleteComplianceDataModel): List<SummaryListRowViewModel> =
            mutableListOf<SummaryListRowViewModel>()
                .apply {
                    addRow(
                        "landlord.incompleteCompliances.summaryRow.propertyAddress",
                        dataModel.singleLineAddress,
                    )
                    addRow(
                        "landlord.incompleteCompliances.summaryRow.localAuthority",
                        dataModel.localAuthorityName,
                    )
                    addRow(
                        "landlord.incompleteCompliances.summaryRow.certificatesDue",
                        dataModel.certificatesDueDate,
                    )
                    addRow(
                        "landlord.incompleteCompliances.summaryRow.gasSafety",
                        getComplianceTaskStatus(dataModel.gasSafety),
                    )
                    addRow(
                        "landlord.incompleteCompliances.summaryRow.electricalSafety",
                        getComplianceTaskStatus(dataModel.electricalSafety),
                    )
                    addRow(
                        "landlord.incompleteCompliances.summaryRow.energyPerformance",
                        getComplianceTaskStatus(dataModel.energyPerformance),
                    )
                    addRow(
                        "landlord.incompleteCompliances.SummaryRow.landlordResponsibilities",
                        getComplianceTaskStatus(dataModel.landlordsResponsibilities),
                    )
                }.toList()

        private fun getActions(dataModel: IncompleteComplianceDataModel): List<SummaryCardActionViewModel> =
            mutableListOf<SummaryCardActionViewModel>()
                .apply {
                    if (dataModel.isComplianceInProgress()) {
                        addAction(
                            "landlord.incompleteCompliances.action.continue",
                            PropertyComplianceController.getPropertyComplianceTaskListPath(dataModel.propertyOwnershipId),
                        )
                    } else {
                        addAction(
                            "landlord.incompleteCompliances.action.start",
                            PropertyComplianceController.getPropertyCompliancePath(dataModel.propertyOwnershipId),
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
}
