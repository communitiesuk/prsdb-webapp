package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_LATER_DEADLINE_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.TagValue
import uk.gov.communities.prsdb.webapp.services.UploadService
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@PrsdbWebService("gasSafetyViewModelServiceRedesign")
class GasSafetyViewModelFactory(
    private val uploadService: UploadService,
    private val messageSource: MessageSource,
) : GasSafetyViewModelService {
    override fun getInsetTextKey(propertyCompliance: PropertyCompliance): String? =
        when {
            propertyCompliance.hasGasSupply == false -> "checkGasSafety.noGasSupplyInsetText"
            propertyCompliance.propertyOwnership.isOccupied && (
                propertyCompliance.isGasSafetyCertExpired == true ||
                    (propertyCompliance.isGasSafetyCertMissing && propertyCompliance.gasSafetyCertProvideLater != true)
            ) -> "checkGasSafety.occupiedNoCertInsetText"
            else -> null
        }

    override fun fromEntity(propertyCompliance: PropertyCompliance): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                if (propertyCompliance.hasGasSupply == false) {
                    addRow(
                        key = "propertyDetails.complianceInformation.gasSafety.hasGasSupply",
                        value = "commonText.no",
                    )
                    return@apply
                }

                if (propertyCompliance.isGasSafetyCertMissing) {
                    addRow(
                        key = "propertyDetails.complianceInformation.gasSafety.hasGasSupply",
                        value = "commonText.yes",
                    )
                    addRow(
                        key = "propertyDetails.complianceInformation.gasSafety.hasValidCert",
                        value = getMissingCertValue(propertyCompliance),
                    )
                    return@apply
                }

                val visibleUploads = propertyCompliance.gasSafetyFileUploads.filter { it.status != FileUploadStatus.DELETED }
                val hasValidCertificate = propertyCompliance.isGasSafetyCertExpired == false

                addRow(
                    key = "propertyDetails.complianceInformation.certificateStatus",
                    value =
                        if (hasValidCertificate) {
                            TagValue("propertyDetails.complianceInformation.valid", "green")
                        } else {
                            TagValue("propertyDetails.complianceInformation.expired", "red")
                        },
                )
                addRow(
                    key = "propertyDetails.complianceInformation.gasSafety.hasValidCert",
                    value = "commonText.yes",
                )

                propertyCompliance.gasSafetyCertIssueDate?.let { issueDate ->
                    addRow(
                        key = "propertyDetails.complianceInformation.issueDate",
                        value = issueDate,
                    )
                }

                if (visibleUploads.isNotEmpty()) {
                    addFileUploadRows(
                        visibleUploads = visibleUploads,
                        certificateKey = "propertyDetails.complianceInformation.gasSafety.yourCertificate",
                        downloadMessageKey =
                            if (hasValidCertificate) {
                                "propertyDetails.complianceInformation.gasSafety.downloadCertificate"
                            } else {
                                "propertyDetails.complianceInformation.gasSafety.downloadExpiredCertificate"
                            },
                        noUploadMessageKey = "",
                        fallbackFileName = "gas_safety_certificate",
                        uploadService = uploadService,
                    )
                }
            }.toList()

    private fun getMissingCertValue(propertyCompliance: PropertyCompliance): Any {
        val isOccupied = propertyCompliance.propertyOwnership.isOccupied
        val isProvideLater = propertyCompliance.gasSafetyCertProvideLater == true

        return when {
            !isOccupied -> "checkGasSafety.provideThisLater.unoccupied"
            isProvideLater -> getProvideLaterWithDeadlineText(propertyCompliance.propertyOwnership.lastOccupiedDate)
            else -> "commonText.no"
        }
    }

    private fun getProvideLaterWithDeadlineText(lastOccupiedDate: LocalDate?): String {
        val deadline = lastOccupiedDate?.plusDays(PROVIDE_LATER_DEADLINE_DAYS) ?: return "checkGasSafety.provideThisLater.occupied"
        val formattedDate = deadline.format(DATE_FORMATTER)
        return messageSource.getMessageForKey(PROVIDE_LATER_WITH_DEADLINE_KEY, arrayOf(formattedDate))
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
        private const val PROVIDE_LATER_WITH_DEADLINE_KEY = "checkGasSafety.provideThisLater.occupiedWithDeadline"
    }
}
