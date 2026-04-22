package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel

class ComplianceActionViewModelBuilder {
    companion object {
        fun fromDataModel(dataModel: ComplianceStatusDataModel): SummaryCardViewModel =
            SummaryCardViewModel(
                title = dataModel.singleLineAddress,
                summaryList = getSummaryList(dataModel),
                actions = getActions(dataModel),
            )

        private fun getSummaryList(dataModel: ComplianceStatusDataModel): List<SummaryListRowViewModel> =
            mutableListOf<SummaryListRowViewModel>()
                .apply {
                    addRow(
                        "complianceActions.summaryRow.registrationNumber",
                        dataModel.registrationNumber,
                    )
                    if (dataModel.shouldShowCert(dataModel.gasSafetyStatus)) {
                        addRow(
                            "complianceActions.summaryRow.gasSafety",
                            MessageKeyConverter.convert(dataModel.gasSafetyStatus),
                        )
                    }
                    if (dataModel.shouldShowCert(dataModel.eicrStatus)) {
                        addRow(
                            "complianceActions.summaryRow.electricalSafety",
                            MessageKeyConverter.convert(dataModel.eicrStatus),
                        )
                    }
                    if (dataModel.shouldShowCert(dataModel.epcStatus)) {
                        addRow(
                            "complianceActions.summaryRow.energyPerformance",
                            MessageKeyConverter.convert(dataModel.epcStatus),
                        )
                    }
                }.toList()

        private fun getActions(dataModel: ComplianceStatusDataModel): List<SummaryCardActionViewModel> =
            listOf(
                SummaryCardActionViewModel(
                    "complianceActions.action.goToProperty",
                    PropertyDetailsController.getPropertyCompliancePath(dataModel.propertyOwnershipId),
                ),
            )
    }
}
