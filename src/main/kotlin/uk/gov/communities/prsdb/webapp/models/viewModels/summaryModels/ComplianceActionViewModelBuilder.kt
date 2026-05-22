package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ComplianceActionViewModelBuilder {
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)

        fun fromDataModel(
            dataModel: ComplianceStatusDataModel,
            useMay26Redesign: Boolean = false,
        ): SummaryCardViewModel =
            SummaryCardViewModel(
                title = dataModel.singleLineAddress,
                summaryList = getSummaryList(dataModel, useMay26Redesign),
                actions = getActions(dataModel),
            )

        private fun getSummaryList(
            dataModel: ComplianceStatusDataModel,
            useMay26Redesign: Boolean,
        ): List<SummaryListRowViewModel> {
            val labelPrefix =
                if (useMay26Redesign) "complianceActions.summaryRow.may26redesign" else "complianceActions.summaryRow.old"

            return mutableListOf<SummaryListRowViewModel>()
                .apply {
                    addRow(
                        "$labelPrefix.registrationNumber",
                        dataModel.registrationNumber,
                    )
                    if (useMay26Redesign) {
                        add(
                            SummaryListRowViewModel(
                                fieldHeading = "$labelPrefix.status",
                                fieldValue =
                                    if (dataModel.isOccupied) {
                                        "$labelPrefix.occupied"
                                    } else {
                                        "$labelPrefix.unoccupied"
                                    },
                                tagColour = if (dataModel.isOccupied) "pink" else "grey",
                            ),
                        )
                    }
                    if (dataModel.shouldShowCert(dataModel.gasSafetyStatus)) {
                        addCertRow(
                            "$labelPrefix.gasSafety",
                            dataModel.gasSafetyStatus,
                            dataModel.provideLaterDeadline,
                            useMay26Redesign,
                        )
                    }
                    if (dataModel.shouldShowCert(dataModel.eicrStatus)) {
                        addCertRow(
                            "$labelPrefix.electricalSafety",
                            dataModel.eicrStatus,
                            dataModel.provideLaterDeadline,
                            useMay26Redesign,
                        )
                    }
                    if (dataModel.shouldShowCert(dataModel.epcStatus)) {
                        addCertRow(
                            "$labelPrefix.energyPerformance",
                            dataModel.epcStatus,
                            dataModel.provideLaterDeadline,
                            useMay26Redesign,
                        )
                    }
                }.toList()
        }

        private fun MutableList<SummaryListRowViewModel>.addCertRow(
            label: String,
            status: ComplianceCertStatus,
            provideLaterDeadline: LocalDate?,
            useMay26Redesign: Boolean,
        ) {
            add(
                SummaryListRowViewModel(
                    fieldHeading = label,
                    fieldValue = getCertStatusValue(status, useMay26Redesign),
                    optionalFieldValueParam = getCertStatusValueParam(status, provideLaterDeadline, useMay26Redesign),
                ),
            )
        }

        private fun getCertStatusValue(
            status: ComplianceCertStatus,
            useMay26Redesign: Boolean,
        ): String {
            val baseKey = MessageKeyConverter.convert(status)
            if (status != ComplianceCertStatus.PROVIDE_LATER) {
                return baseKey
            }
            val suffix = if (useMay26Redesign) "may26Redesign" else "old"
            return "$baseKey.$suffix"
        }

        private fun getCertStatusValueParam(
            status: ComplianceCertStatus,
            provideLaterDeadline: LocalDate?,
            useMay26Redesign: Boolean,
        ): String? {
            if (status != ComplianceCertStatus.PROVIDE_LATER || !useMay26Redesign) {
                return null
            }
            return provideLaterDeadline?.format(DATE_FORMATTER)
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
