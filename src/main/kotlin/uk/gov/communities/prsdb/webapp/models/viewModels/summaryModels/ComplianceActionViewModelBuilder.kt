package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.constants.TAG_COLOUR_GRAY
import uk.gov.communities.prsdb.webapp.constants.TAG_COLOUR_PINK
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel

class ComplianceActionViewModelBuilderOld {
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
                        "complianceActions.summaryRow.old.registrationNumber",
                        dataModel.registrationNumber,
                    )
                    if (dataModel.shouldShowCert(dataModel.gasSafetyStatus)) {
                        addRow(
                            "complianceActions.summaryRow.old.gasSafety",
                            MessageKeyConverter.convert(dataModel.gasSafetyStatus),
                        )
                    }
                    if (dataModel.shouldShowCert(dataModel.eicrStatus)) {
                        addRow(
                            "complianceActions.summaryRow.old.electricalSafety",
                            MessageKeyConverter.convert(dataModel.eicrStatus),
                        )
                    }
                    if (dataModel.shouldShowCert(dataModel.epcStatus)) {
                        addRow(
                            "complianceActions.summaryRow.old.energyPerformance",
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

class ComplianceActionViewModelBuilderMay26Redesign {
    companion object {
        val OCCUPIED_TAG_COLOUR = TAG_COLOUR_PINK
        val UNOCCUPIED_TAG_COLOUR = TAG_COLOUR_GRAY

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
                        "complianceActions.summaryRow.may26redesign.registrationNumber",
                        dataModel.registrationNumber,
                    )
                    add(
                        SummaryListRowViewModel(
                            fieldHeading = "complianceActions.summaryRow.may26redesign.status",
                            fieldValue =
                                if (dataModel.isOccupied) {
                                    "complianceActions.summaryRow.may26redesign.occupied"
                                } else {
                                    "complianceActions.summaryRow.may26redesign.unoccupied"
                                },
                            tagColour = if (dataModel.isOccupied) OCCUPIED_TAG_COLOUR else UNOCCUPIED_TAG_COLOUR,
                        ),
                    )
                    if (dataModel.shouldShowCert(dataModel.gasSafetyStatus)) {
                        addRow(
                            "complianceActions.summaryRow.may26redesign.gasSafety",
                            MessageKeyConverter.convert(dataModel.gasSafetyStatus),
                        )
                    }
                    if (dataModel.shouldShowCert(dataModel.eicrStatus)) {
                        addRow(
                            "complianceActions.summaryRow.may26redesign.electricalSafety",
                            MessageKeyConverter.convert(dataModel.eicrStatus),
                        )
                    }
                    if (dataModel.shouldShowCert(dataModel.epcStatus)) {
                        addRow(
                            "complianceActions.summaryRow.may26redesign.energyPerformance",
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
