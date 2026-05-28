package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.constants.TAG_COLOUR_GREY
import uk.gov.communities.prsdb.webapp.constants.TAG_COLOUR_PINK
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
                            "${MessageKeyConverter.convert(dataModel.gasSafetyStatus)}.old",
                        )
                    }
                    if (dataModel.shouldShowCert(dataModel.eicrStatus)) {
                        addRow(
                            "complianceActions.summaryRow.old.electricalSafety",
                            "${MessageKeyConverter.convert(dataModel.eicrStatus)}.old",
                        )
                    }
                    if (dataModel.shouldShowCert(dataModel.epcStatusOld)) {
                        addRow(
                            "complianceActions.summaryRow.old.energyPerformance",
                            "${MessageKeyConverter.convert(dataModel.epcStatusOld)}.old",
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
        val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
        val OCCUPIED_TAG_COLOUR = TAG_COLOUR_PINK
        val UNOCCUPIED_TAG_COLOUR = TAG_COLOUR_GREY

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
                        addCertRow(
                            "complianceActions.summaryRow.may26redesign.gasSafety",
                            dataModel.gasSafetyStatus,
                            "gasSafety",
                            dataModel.provideLaterDeadline,
                            dataModel.gasSafetyExpiryDate,
                        )
                    }
                    if (dataModel.shouldShowCert(dataModel.eicrStatus)) {
                        addCertRow(
                            "complianceActions.summaryRow.may26redesign.electricalSafety",
                            dataModel.eicrStatus,
                            "electricalSafety",
                            dataModel.provideLaterDeadline,
                            dataModel.eicrExpiryDate,
                        )
                    }
                    if (dataModel.shouldShowCert(dataModel.epcStatusMay2026Redesign)) {
                        addCertRow(
                            "complianceActions.summaryRow.may26redesign.energyPerformance",
                            dataModel.epcStatusMay2026Redesign,
                            "epc",
                            dataModel.provideLaterDeadline,
                            dataModel.epcExpiryDate,
                        )
                    }
                }.toList()

        private fun MutableList<SummaryListRowViewModel>.addCertRow(
            label: String,
            status: ComplianceCertStatus,
            certTypeKey: String,
            provideLaterDeadline: LocalDate?,
            expiryDate: LocalDate?,
        ) {
            add(
                SummaryListRowViewModel(
                    fieldHeading = label,
                    fieldValue = getCertStatusValue(status, certTypeKey),
                    optionalFieldValueParam = getCertStatusValueParam(status, provideLaterDeadline, expiryDate),
                ),
            )
        }

        private fun getCertStatusValue(
            status: ComplianceCertStatus,
            certTypeKey: String,
        ): String {
            val baseKey = MessageKeyConverter.convert(status)
            return when (status) {
                ComplianceCertStatus.HAS_FAULTS -> {
                    "$baseKey.may26Redesign.$certTypeKey"
                }

                ComplianceCertStatus.PROVIDE_LATER, ComplianceCertStatus.EXPIRED -> {
                    "$baseKey.may26Redesign"
                }

                else -> {
                    baseKey
                }
            }
        }

        private fun getCertStatusValueParam(
            status: ComplianceCertStatus,
            provideLaterDeadline: LocalDate?,
            expiryDate: LocalDate?,
        ): String? =
            when (status) {
                ComplianceCertStatus.PROVIDE_LATER -> provideLaterDeadline?.format(DATE_FORMATTER)
                ComplianceCertStatus.EXPIRED -> expiryDate?.format(DATE_FORMATTER)
                else -> null
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
