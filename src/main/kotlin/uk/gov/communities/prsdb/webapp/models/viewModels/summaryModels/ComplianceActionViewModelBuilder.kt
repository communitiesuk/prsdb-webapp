package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor.Companion.overrideBackLinkForUrl
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel

class ComplianceActionViewModelBuilder {
    companion object {
        fun fromDataModel(
            dataModel: ComplianceStatusDataModel,
            currentUrlKey: Int,
        ): SummaryCardViewModel =
            SummaryCardViewModel(
                title = dataModel.singleLineAddress,
                summaryList = getSummaryList(dataModel),
                actions = getActions(dataModel, currentUrlKey),
            )

        private fun getSummaryList(dataModel: ComplianceStatusDataModel): List<SummaryListRowViewModel> =
            mutableListOf<SummaryListRowViewModel>()
                .apply {
                    addRow(
                        "complianceActions.summaryRow.registrationNumber",
                        dataModel.registrationNumber,
                    )
                    if (!dataModel.isComplete || dataModel.gasSafetyStatus != ComplianceCertStatus.ADDED) {
                        addRow(
                            "complianceActions.summaryRow.gasSafety",
                            MessageKeyConverter.convert(dataModel.gasSafetyStatus),
                        )
                    }
                    if (!dataModel.isComplete || dataModel.eicrStatus != ComplianceCertStatus.ADDED) {
                        addRow(
                            "complianceActions.summaryRow.electricalSafety",
                            MessageKeyConverter.convert(dataModel.eicrStatus),
                        )
                    }
                    if (!dataModel.isComplete || dataModel.epcStatus != ComplianceCertStatus.ADDED) {
                        addRow(
                            "complianceActions.summaryRow.energyPerformance",
                            MessageKeyConverter.convert(dataModel.epcStatus),
                        )
                    }
                }.toList()

        private fun getActions(
            dataModel: ComplianceStatusDataModel,
            currentUrlKey: Int?,
        ): List<SummaryCardActionViewModel> {
            val action =
                if (dataModel.isComplete) {
                    SummaryCardActionViewModel(
                        "complianceActions.action.update",
                        PropertyDetailsController
                            .getPropertyCompliancePath(dataModel.propertyOwnershipId)
                            .overrideBackLinkForUrl(currentUrlKey),
                    )
                } else if (dataModel.isInProgress) {
                    SummaryCardActionViewModel(
                        "complianceActions.action.continue",
                        PropertyComplianceController
                            .getPropertyComplianceTaskListPath(dataModel.propertyOwnershipId)
                            .overrideBackLinkForUrl(currentUrlKey),
                    )
                } else {
                    SummaryCardActionViewModel(
                        "complianceActions.action.start",
                        PropertyComplianceController.getPropertyCompliancePath(dataModel.propertyOwnershipId),
                    )
                }

            return listOf(action)
        }
    }
}
