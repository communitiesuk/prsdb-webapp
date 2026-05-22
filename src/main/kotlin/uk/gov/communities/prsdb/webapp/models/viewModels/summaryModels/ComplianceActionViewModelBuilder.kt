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
                            "gasSafety",
                            dataModel.provideLaterDeadline,
                            dataModel.gasSafetyExpiryDate,
                            useMay26Redesign,
                        )
                    }
                    if (dataModel.shouldShowCert(dataModel.eicrStatus)) {
                        addCertRow(
                            "$labelPrefix.electricalSafety",
                            dataModel.eicrStatus,
                            "electricalSafety",
                            dataModel.provideLaterDeadline,
                            dataModel.eicrExpiryDate,
                            useMay26Redesign,
                        )
                    }
                    if (dataModel.shouldShowCert(dataModel.epcStatus)) {
                        addCertRow(
                            "$labelPrefix.energyPerformance",
                            dataModel.epcStatus,
                            "epc",
                            dataModel.provideLaterDeadline,
                            dataModel.epcExpiryDate,
                            useMay26Redesign,
                        )
                    }
                }.toList()
        }

        private fun MutableList<SummaryListRowViewModel>.addCertRow(
            label: String,
            status: ComplianceCertStatus,
            certTypeKey: String,
            provideLaterDeadline: LocalDate?,
            expiryDate: LocalDate?,
            useMay26Redesign: Boolean,
        ) {
            add(
                SummaryListRowViewModel(
                    fieldHeading = label,
                    fieldValue = getCertStatusValue(status, certTypeKey, useMay26Redesign),
                    optionalFieldValueParam = getCertStatusValueParam(status, provideLaterDeadline, expiryDate, useMay26Redesign),
                ),
            )
        }

        private fun getCertStatusValue(
            status: ComplianceCertStatus,
            certTypeKey: String,
            useMay26Redesign: Boolean,
        ): String {
            val baseKey = MessageKeyConverter.convert(status)
            return when (status) {
                ComplianceCertStatus.NOT_ADDED ->
                    if (useMay26Redesign) "$baseKey.may26Redesign.$certTypeKey" else "$baseKey.old"
                ComplianceCertStatus.PROVIDE_LATER, ComplianceCertStatus.EXPIRED -> {
                    val suffix = if (useMay26Redesign) "may26Redesign" else "old"
                    "$baseKey.$suffix"
                }
                else -> baseKey
            }
        }

        private fun getCertStatusValueParam(
            status: ComplianceCertStatus,
            provideLaterDeadline: LocalDate?,
            expiryDate: LocalDate?,
            useMay26Redesign: Boolean,
        ): String? {
            if (!useMay26Redesign) return null
            return when (status) {
                ComplianceCertStatus.PROVIDE_LATER -> provideLaterDeadline?.format(DATE_FORMATTER)
                ComplianceCertStatus.EXPIRED -> expiryDate?.format(DATE_FORMATTER)
                else -> null
            }
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
