package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.UploadService

@PrsdbWebService
class GasSafetyViewModelFactory(
    private val uploadService: UploadService,
) {
    fun fromEntity(
        propertyCompliance: PropertyCompliance,
        withActionLinks: Boolean,
    ): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    key = "propertyDetails.complianceInformation.gasSafety.gasSafetyCertificate",
                    value = getGasCertificateMessageKey(propertyCompliance),
                    valueUrl =
                        propertyCompliance.gasSafetyFileUpload?.let {
                            uploadService.getDownloadUrlOrNull(it, "gas_safety_certificate.${it.extension}")
                        },
                    actionText = "forms.links.change",
                    actionLink =
                        PropertyComplianceController.getUpdatePropertyComplianceStepPath(
                            propertyCompliance.propertyOwnership.id,
                            PropertyComplianceStepId.UpdateGasSafety,
                        ),
                    withActionLink = withActionLinks,
                )
                if (propertyCompliance.gasSafetyCertIssueDate != null) {
                    addRow(
                        key = "propertyDetails.complianceInformation.issueDate",
                        value = propertyCompliance.gasSafetyCertIssueDate,
                    )
                    addRow(
                        key = "propertyDetails.complianceInformation.validUntil",
                        value = propertyCompliance.gasSafetyCertExpiryDate,
                    )
                    addRow(
                        key = "propertyDetails.complianceInformation.gasSafety.gasSafeEngineerNumber",
                        value = propertyCompliance.gasSafetyCertEngineerNum,
                    )
                } else {
                    addRow(
                        key = "propertyDetails.complianceInformation.exemption",
                        value =
                            getExemptionReasonValue(
                                propertyCompliance.gasSafetyCertExemptionReason,
                                propertyCompliance.gasSafetyCertExemptionOtherReason,
                            ),
                    )
                }
            }.toList()

    private fun getGasCertificateMessageKey(propertyCompliance: PropertyCompliance): String {
        val uploadedFileStatus = propertyCompliance.gasSafetyFileUpload?.status
        val expired = propertyCompliance.isGasSafetyCertExpired
        return when {
            uploadedFileStatus == FileUploadStatus.SCANNED && !expired!! ->
                "propertyDetails.complianceInformation.gasSafety.downloadCertificate"

            uploadedFileStatus == FileUploadStatus.SCANNED && expired!! ->
                "propertyDetails.complianceInformation.gasSafety.downloadExpiredCertificate"

            uploadedFileStatus == FileUploadStatus.QUARANTINED ->
                "propertyCompliance.uploadedFile.virusScanPending"

            uploadedFileStatus == FileUploadStatus.DELETED ->
                "propertyCompliance.uploadedFile.virusScanFailed"

            expired == true ->
                "propertyDetails.complianceInformation.expired"

            propertyCompliance.hasGasSafetyExemption ->
                "propertyDetails.complianceInformation.exempt"

            else ->
                "propertyDetails.complianceInformation.notAdded"
        }
    }

    private fun getExemptionReasonValue(
        exemptionReason: GasSafetyExemptionReason?,
        exemptionOtherReason: String?,
    ): Any =
        when (exemptionReason) {
            null -> "propertyDetails.complianceInformation.noExemption"
            GasSafetyExemptionReason.OTHER -> listOf(MessageKeyConverter.convert(GasSafetyExemptionReason.OTHER), exemptionOtherReason)
            else -> MessageKeyConverter.convert(exemptionReason)
        }
}
