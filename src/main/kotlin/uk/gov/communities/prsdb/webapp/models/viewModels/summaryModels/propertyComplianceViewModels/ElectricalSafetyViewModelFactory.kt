package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.TagValue
import uk.gov.communities.prsdb.webapp.services.UploadService

@PrsdbWebService("electricalSafetyViewModelServiceRedesign")
class ElectricalSafetyViewModelFactory(
    private val uploadService: UploadService,
    messageSource: MessageSource,
) : ComplianceViewModelServiceBase(messageSource),
    ElectricalSafetyViewModelService {
    override val provideLaterUnoccupiedKey = "checkElectricalSafety.provideThisLater.unoccupied"
    override val provideLaterWithDeadlineKey = "checkElectricalSafety.provideThisLater.occupiedWithDeadline"
    override val missingCertOccupiedValue = "commonText.none"

    override fun getInsetTextKey(propertyCompliance: PropertyCompliance): String? {
        val status = ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance).electricalSafetyStatus
        return when {
            propertyCompliance.propertyOwnership.isOccupied &&
                status in ComplianceCertStatus.COUNCIL_WILL_SEE_STATUSES -> {
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
                val status = ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance).electricalSafetyStatus

                when (status) {
                    in ComplianceCertStatus.NEEDS_COMPLIANCE_IF_OCCUPIED_STATUSES -> {
                        val isProvideLater =
                            !propertyCompliance.propertyOwnership.isOccupied ||
                                status == ComplianceCertStatus.PROVIDE_LATER
                        val key =
                            if (isProvideLater) {
                                "propertyDetails.complianceInformation.electricalSafety.whichCertificateDoes"
                            } else {
                                "propertyDetails.complianceInformation.electricalSafety.whichCertificate"
                            }
                        addRow(
                            key = key,
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
                            TagValue.VALID
                        } else {
                            TagValue.EXPIRED
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

    companion object {
        private const val DEFAULT_CERT_KEY_PREFIX = "propertyDetails.complianceInformation.electricalSafety"
    }
}
