package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor.Companion.overrideBackLinkForUrl
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.OutstandingActionsDataModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class OutstandingActionViewModelBuilder {
    companion object {
        val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)

        fun fromDataModel(
            dataModel: OutstandingActionsDataModel,
            currentUrlKey: Int? = null,
        ): SummaryCardViewModel =
            SummaryCardViewModel(
                title = dataModel.singleLineAddress,
                summaryList = getSummaryList(dataModel),
                actions = getActions(dataModel, currentUrlKey),
                insetViewModel = dataModel.complianceStatus?.let { getInsetViewModel(it) },
            )

        private fun getSummaryList(dataModel: OutstandingActionsDataModel): List<SummaryListRowViewModel> =
            mutableListOf<SummaryListRowViewModel>()
                .apply {
                    if (dataModel.shouldShowLicensingAction) {
                        addProvideLaterRow("outstandingActions.summaryRow.licensing", dataModel.provideLaterDeadline)
                    }
                    if (dataModel.shouldShowTenancyAction) {
                        addProvideLaterRow("outstandingActions.summaryRow.tenancy", dataModel.provideLaterDeadline)
                    }
                    dataModel.complianceStatus?.let { addCertRows(it) }
                }.toList()

        private fun MutableList<SummaryListRowViewModel>.addProvideLaterRow(
            label: String,
            provideLaterDeadline: LocalDate?,
        ) {
            add(
                SummaryListRowViewModel(
                    fieldHeading = label,
                    fieldValue =
                        if (provideLaterDeadline == null) {
                            "outstandingActions.status.provideLaterNoDeadline"
                        } else {
                            "outstandingActions.status.provideLater"
                        },
                    optionalFieldValueParam = provideLaterDeadline?.format(DATE_FORMATTER),
                ),
            )
        }

        private fun MutableList<SummaryListRowViewModel>.addCertRows(complianceStatus: ComplianceStatusDataModel) {
            if (complianceStatus.shouldShowGasSafetyAction()) {
                addCertRow(
                    "outstandingActions.summaryRow.gasSafety",
                    getCertStatusDisplay(
                        complianceStatus.gasSafetyStatus,
                        "gasSafety",
                        complianceStatus.provideLaterDeadline,
                        complianceStatus.gasSafetyExpiryDate,
                    ),
                )
            }
            if (complianceStatus.shouldShowElectricalSafetyAction()) {
                addCertRow(
                    "outstandingActions.summaryRow.electricalSafety",
                    getElectricalSafetyStatusDisplay(complianceStatus),
                )
            }
            if (complianceStatus.shouldShowEpcAction()) {
                addCertRow(
                    "outstandingActions.summaryRow.energyPerformance",
                    getCertStatusDisplay(
                        complianceStatus.epcStatus,
                        "epc",
                        complianceStatus.provideLaterDeadline,
                        complianceStatus.epcExpiryDate,
                    ),
                )
            }
        }

        private fun MutableList<SummaryListRowViewModel>.addCertRow(
            label: String,
            certStatusDisplay: CertStatusDisplay,
        ) {
            add(
                SummaryListRowViewModel(
                    fieldHeading = label,
                    fieldValue = certStatusDisplay.messageKey,
                    optionalFieldValueParam = certStatusDisplay.date?.format(DATE_FORMATTER),
                ),
            )
        }

        private fun getElectricalSafetyStatusDisplay(complianceStatus: ComplianceStatusDataModel): CertStatusDisplay {
            val eicIssueDate = complianceStatus.eicIssueDate
            return if (complianceStatus.electricalSafetyStatus == ComplianceCertStatus.EXPIRED && eicIssueDate != null) {
                CertStatusDisplay("outstandingActions.status.expiredEic", eicIssueDate)
            } else {
                getCertStatusDisplay(
                    complianceStatus.electricalSafetyStatus,
                    "electricalSafety",
                    complianceStatus.provideLaterDeadline,
                    complianceStatus.electricalSafetyExpiryDate,
                )
            }
        }

        private fun getCertStatusDisplay(
            status: ComplianceCertStatus,
            certTypeKey: String,
            provideLaterDeadline: LocalDate?,
            expiryDate: LocalDate?,
        ): CertStatusDisplay =
            when (status) {
                ComplianceCertStatus.HAS_FAULTS -> {
                    CertStatusDisplay("outstandingActions.status.hasFaults.$certTypeKey")
                }

                ComplianceCertStatus.PROVIDE_LATER -> {
                    if (provideLaterDeadline == null) {
                        CertStatusDisplay("outstandingActions.status.provideLaterNoDeadline")
                    } else {
                        CertStatusDisplay("outstandingActions.status.provideLater", provideLaterDeadline)
                    }
                }

                ComplianceCertStatus.EXPIRED -> {
                    CertStatusDisplay("outstandingActions.status.expired", expiryDate)
                }

                else -> {
                    throw IllegalStateException("${status.name} should not be shown on the outstanding actions page")
                }
            }

        private data class CertStatusDisplay(
            val messageKey: String,
            val date: LocalDate? = null,
        )

        private fun getInsetViewModel(dataModel: ComplianceStatusDataModel): ComplianceActionInsetViewModel? =
            if (dataModel.epcStatus == ComplianceCertStatus.EXPIRED && dataModel.isOccupied &&
                dataModel.tenancyStartedBeforeEpcExpiry
            ) {
                if (dataModel.epcExpiryDate == null) throw PrsdbWebException("epcExpiryDate was null for an expired certificate")
                ComplianceActionInsetViewModel(
                    expiryDate = dataModel.epcExpiryDate.format(DATE_FORMATTER),
                    linkUrl = GET_NEW_EPC_URL,
                )
            } else {
                null
            }

        private fun getActions(
            dataModel: OutstandingActionsDataModel,
            currentUrlKey: Int?,
        ): List<SummaryCardActionViewModel> =
            listOf(
                SummaryCardActionViewModel(
                    "outstandingActions.action.goToProperty",
                    if (dataModel.hasPropertyDetailsAction) {
                        PropertyDetailsController.getPropertyDetailsPath(dataModel.propertyOwnershipId)
                    } else {
                        PropertyDetailsController.getPropertyCompliancePath(dataModel.propertyOwnershipId)
                    }.overrideBackLinkForUrl(currentUrlKey),
                ),
            )
    }
}
