package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_LATER_DEADLINE_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.TagValue
import uk.gov.communities.prsdb.webapp.services.UploadService
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@PrsdbWebService("electricalSafetyViewModelServiceRedesign")
class ElectricalSafetyViewModelServiceRedesign(
    private val uploadService: UploadService,
    private val messageSource: MessageSource,
) : ElectricalSafetyViewModelService {
    override fun getInsetTextKey(propertyCompliance: PropertyCompliance): String? {
        val status = ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance).eicrStatus
        return when {
            propertyCompliance.propertyOwnership.isOccupied &&
                status in listOf(ComplianceCertStatus.EXPIRED, ComplianceCertStatus.NOT_ADDED) -> {
                "checkElectricalSafety.occupiedNoCertInsetText"
            }

            else -> {
                null
            }
        }
    }

    override fun fromEntity(propertyCompliance: PropertyCompliance): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val status = ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance).eicrStatus

                when (status) {
                    in ComplianceCertStatus.MISSING_STATUSES -> {
                        addRow(
                            key = "propertyDetails.complianceInformation.electricalSafety.whichCertificate",
                            value = getMissingCertValue(status, propertyCompliance),
                        )
                        return@apply
                    }

                    else -> {}
                }

                val hasValidCertificate = status == ComplianceCertStatus.ADDED

                addRow(
                    key = "propertyDetails.complianceInformation.certificateStatus",
                    value =
                        if (hasValidCertificate) {
                            TagValue("propertyDetails.complianceInformation.valid", "green")
                        } else {
                            TagValue("propertyDetails.complianceInformation.expired", "red")
                        },
                )

                val certKeyPrefix = getCertKeyPrefix(propertyCompliance.electricalCertType)
                if (propertyCompliance.electricalCertType != null) {
                    addRow(
                        key = "propertyDetails.complianceInformation.electricalSafety.whichCertificate",
                        value = "$certKeyPrefix.certificate",
                    )
                }

                propertyCompliance.electricalSafetyExpiryDate?.let { expiryDate ->
                    addRow(
                        key = "propertyDetails.complianceInformation.expiryDate",
                        value = expiryDate,
                    )
                }

                val visibleUploads =
                    propertyCompliance.electricalSafetyFileUploads.filter { it.status != FileUploadStatus.DELETED }
                if (visibleUploads.isNotEmpty()) {
                    addFileUploadRows(
                        visibleUploads = visibleUploads,
                        certificateKey = "propertyDetails.complianceInformation.electricalSafety.yourCertificate",
                        downloadMessageKey =
                            if (hasValidCertificate) {
                                "$certKeyPrefix.downloadCertificate"
                            } else {
                                "$certKeyPrefix.downloadExpiredCertificate"
                            },
                        noUploadMessageKey = "",
                        fallbackFileName = "electrical_safety_certificate",
                        uploadService = uploadService,
                    )
                }
            }.toList()

    private fun getCertKeyPrefix(electricalCertType: CertificateType?): String =
        when (electricalCertType) {
            CertificateType.Eic -> "propertyDetails.complianceInformation.electricalSafety.eic"
            CertificateType.Eicr -> "propertyDetails.complianceInformation.electricalSafety.eicr"
            else -> DEFAULT_CERT_KEY_PREFIX
        }

    private fun getMissingCertValue(
        status: ComplianceCertStatus,
        propertyCompliance: PropertyCompliance,
    ): Any {
        val isOccupied = propertyCompliance.propertyOwnership.isOccupied

        return when {
            !isOccupied -> {
                "checkElectricalSafety.provideThisLater.unoccupied"
            }

            status == ComplianceCertStatus.PROVIDE_LATER -> {
                getProvideLaterWithDeadlineText(propertyCompliance.propertyOwnership.lastOccupiedDate)
            }

            else -> {
                "commonText.none"
            }
        }
    }

    private fun getProvideLaterWithDeadlineText(lastOccupiedDate: LocalDate?): String {
        val deadline =
            lastOccupiedDate?.plusDays(PROVIDE_LATER_DEADLINE_DAYS.toLong()) ?: return "checkElectricalSafety.provideThisLater.occupied"
        val formattedDate = deadline.format(DATE_FORMATTER)
        return messageSource.getMessageForKey(PROVIDE_LATER_WITH_DEADLINE_KEY, arrayOf(formattedDate))
    }

    companion object {
        private const val DEFAULT_CERT_KEY_PREFIX = "propertyDetails.complianceInformation.electricalSafety"
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
        private const val PROVIDE_LATER_WITH_DEADLINE_KEY = "checkElectricalSafety.provideThisLater.occupiedWithDeadline"
    }
}
