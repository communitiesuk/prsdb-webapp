package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel

class ComplianceActionViewModelBuilder {
    companion object {
        fun fromDataModel(
            dataModel: ComplianceStatusDataModel,
            useRedesignedLabels: Boolean = false,
        ): SummaryCardViewModel =
            SummaryCardViewModel(
                title = dataModel.singleLineAddress,
                summaryList = getSummaryList(dataModel, useRedesignedLabels),
                actions = getActions(dataModel),
            )

        private fun getSummaryList(
            dataModel: ComplianceStatusDataModel,
            useRedesignedLabels: Boolean,
        ): List<SummaryListRowViewModel> {
            val labelPrefix =
                if (useRedesignedLabels) "complianceActions.summaryRow.may26redesign" else "complianceActions.summaryRow.old"

            return mutableListOf<SummaryListRowViewModel>()
                .apply {
                    addRow(
                        "$labelPrefix.registrationNumber",
                        dataModel.registrationNumber,
                    )
                    if (dataModel.shouldShowCert(dataModel.gasSafetyStatus)) {
                        addRow(
                            "$labelPrefix.gasSafety",
                            MessageKeyConverter.convert(dataModel.gasSafetyStatus),
                        )
                    }
                    if (dataModel.shouldShowCert(dataModel.eicrStatus)) {
                        addRow(
                            "$labelPrefix.electricalSafety",
                            MessageKeyConverter.convert(dataModel.eicrStatus),
                        )
                    }
                    if (dataModel.shouldShowCert(dataModel.epcStatus)) {
                        addRow(
                            "$labelPrefix.energyPerformance",
                            MessageKeyConverter.convert(dataModel.epcStatus),
                        )
                    }
                }.toList()
        }

        private fun getActions(dataModel: ComplianceStatusDataModel): List<SummaryCardActionViewModel> =
            listOf(
                SummaryCardActionViewModel(
                    "complianceActions.action.goToProperty",
                    PropertyDetailsController.getPropertyCompliancePath(dataModel.propertyOwnershipId),
                ),
            )
    }
}
