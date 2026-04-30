package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.RichTextValue
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.UploadService

fun MutableList<SummaryListRowViewModel>.addFileUploadRows(
    visibleUploads: List<FileUpload>,
    certificateKey: String,
    downloadMessageKey: String,
    noUploadMessageKey: String,
    fallbackFileName: String,
    uploadService: UploadService,
) {
    if (visibleUploads.isEmpty()) {
        addRow(key = certificateKey, value = noUploadMessageKey)
        return
    }

    val values =
        visibleUploads.mapNotNull { upload ->
            val displayName = upload.fileName ?: "$fallbackFileName.${upload.extension}"
            when (upload.status) {
                FileUploadStatus.SCANNED ->
                    RichTextValue(
                        textKey = downloadMessageKey,
                        url = uploadService.getDownloadUrlOrNull(upload, displayName),
                    )

                FileUploadStatus.QUARANTINED ->
                    RichTextValue(
                        textKey = VIRUS_SCAN_PENDING_WITH_NAME_KEY,
                        optionalParam = displayName,
                    )

                else -> null
            }
        }

    if (values.isNotEmpty()) {
        addRow(key = certificateKey, value = values)
    }
}

private const val VIRUS_SCAN_PENDING_WITH_NAME_KEY = "propertyCompliance.uploadedFile.virusScanPendingWithName"
