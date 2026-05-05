package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.UploadService

@PrsdbWebService
class GasSafetyViewModelFactory(
    private val uploadService: UploadService,
) {
    fun fromEntity(propertyCompliance: PropertyCompliance): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val visibleUploads = propertyCompliance.gasSafetyFileUploads.filter { it.status != FileUploadStatus.DELETED }
                val expired = propertyCompliance.isGasSafetyCertExpired
                addFileUploadRows(
                    visibleUploads = visibleUploads,
                    certificateKey = "propertyDetails.complianceInformation.gasSafety.gasSafetyCertificate",
                    downloadMessageKey =
                        if (expired == true) {
                            "propertyDetails.complianceInformation.gasSafety.downloadExpiredCertificate"
                        } else {
                            "propertyDetails.complianceInformation.gasSafety.downloadCertificate"
                        },
                    noUploadMessageKey = getNonUploadStatusMessageKey(propertyCompliance),
                    fallbackFileName = "gas_safety_certificate",
                    uploadService = uploadService,
                )
                if (propertyCompliance.gasSafetyCertIssueDate != null) {
                    addRow(
                        key = "propertyDetails.complianceInformation.issueDate",
                        value = propertyCompliance.gasSafetyCertIssueDate,
                    )
                }
            }.toList()

    private fun getNonUploadStatusMessageKey(propertyCompliance: PropertyCompliance): String =
        when {
            propertyCompliance.isGasSafetyCertExpired == true -> "propertyDetails.complianceInformation.expired"
            else -> "propertyDetails.complianceInformation.notAdded"
        }
}
