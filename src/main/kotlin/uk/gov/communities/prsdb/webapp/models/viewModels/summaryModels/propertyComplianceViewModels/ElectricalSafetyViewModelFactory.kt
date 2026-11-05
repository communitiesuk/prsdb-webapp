package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.EIC_ISSUE_DATE
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.TagValue
import uk.gov.communities.prsdb.webapp.services.EicDateService
import uk.gov.communities.prsdb.webapp.services.UploadService

@PrsdbWebService
class ElectricalSafetyViewModelFactory(
    private val uploadService: UploadService,
    messageSource: MessageSource,
    private val eicDateService: EicDateService,
    // TODO PDJB-1679: Remove featureFlagManager when the EIC_ISSUE_DATE flag is removed.
    private val featureFlagManager: FeatureFlagManager,
) : ComplianceViewModelFactoryBase(messageSource) {
    override val provideLaterUnoccupiedKey = "checkElectricalSafety.provideThisLater.unoccupied"
    override val provideLaterNoDeadlineKey = "checkElectricalSafety.provideThisLater.occupiedNoDeadline"
    override val provideLaterWithDeadlineKey = "checkElectricalSafety.provideThisLater.occupiedWithDeadline"
    override val missingCertOccupiedValue = "commonText.none"
    override val occupiedNoCertInsetKey = "checkElectricalSafety.occupiedNoCertInsetText"

    fun getInsetTextKey(
        propertyCompliance: PropertyCompliance,
        complianceStatus: ComplianceStatusDataModel,
    ): String? = getCouncilWillSeeInsetKey(complianceStatus.electricalSafetyStatus, propertyCompliance)

    fun fromEntity(
        propertyCompliance: PropertyCompliance,
        complianceStatus: ComplianceStatusDataModel,
    ): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val status = complianceStatus.electricalSafetyStatus

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

                when {
                    // TODO PDJB-1679: Remove featureFlagManager.checkFeature(EIC_ISSUE_DATE).
                    propertyCompliance.electricalCertType == CertificateType.Eic && featureFlagManager.checkFeature(EIC_ISSUE_DATE) -> {
                        // TODO PDJB-1673: Tidy up EIC issue-date logic when issue dates are stored directly.
                        eicDateService.issueDateFrom(propertyCompliance)?.let { issueDate ->
                            addRow(
                                key = "propertyDetails.complianceInformation.issueDate",
                                value = issueDate,
                            )
                        }
                    }

                    else -> {
                        complianceStatus.electricalSafetyExpiryDate?.let { expiryDate ->
                            addRow(
                                key = "propertyDetails.complianceInformation.expiryDate",
                                value = expiryDate,
                            )
                        }
                    }
                }

                val (uploadsDeletedByVirusScan, visibleUploads) =
                    propertyCompliance.electricalSafetyFileUploads.partition { it.status == FileUploadStatus.DELETED }

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
                } else if (uploadsDeletedByVirusScan.isNotEmpty()) {
                    addRow(
                        key = "propertyDetails.complianceInformation.electricalSafety.yourCertificate",
                        value = "propertyCompliance.uploadedFile.virusScanFailed",
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
