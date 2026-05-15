package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.UploadedFileUrl
import uk.gov.communities.prsdb.webapp.services.UploadService

@ExtendWith(MockitoExtension::class)
class ComplianceUploadRowHelperTests {
    private val mockUploadService: UploadService = mock()

    private fun fileUploadWithStatus(status: FileUploadStatus): FileUpload =
        mock<FileUpload>().also { whenever(it.status).thenReturn(status) }

    private fun scannedUpload(
        displayName: String,
        downloadUrl: String?,
    ): Pair<FileUpload, String> {
        val fileUpload = fileUploadWithStatus(FileUploadStatus.SCANNED)
        whenever(mockUploadService.getDownloadUrlOrNull(eq(fileUpload), any())).thenReturn(downloadUrl)
        return fileUpload to displayName
    }

    private fun quarantinedUpload(displayName: String): Pair<FileUpload, String> =
        fileUploadWithStatus(FileUploadStatus.QUARANTINED) to displayName

    private fun deletedUpload(displayName: String): Pair<FileUpload, String> = fileUploadWithStatus(FileUploadStatus.DELETED) to displayName

    @Test
    fun `toUploadedFileUrls maps scanned uploads to UploadedFileUrl with download messageKey and url`() {
        val uploads = listOf(scannedUpload("cert.pdf", "/download/cert.pdf"))

        val result = uploads.toUploadedFileUrls("download.messageKey", mockUploadService)

        assertEquals(
            listOf(
                UploadedFileUrl(
                    messageKey = "download.messageKey",
                    displayName = "cert.pdf",
                    url = "/download/cert.pdf",
                ),
            ),
            result,
        )
    }

    @Test
    fun `toUploadedFileUrls maps scanned uploads with no download url to UploadedFileUrl with null url`() {
        val uploads = listOf(scannedUpload("cert.pdf", null))

        val result = uploads.toUploadedFileUrls("download.messageKey", mockUploadService)

        assertEquals(1, result.size)
        assertEquals("download.messageKey", result[0].messageKey)
        assertEquals("cert.pdf", result[0].displayName)
        assertNull(result[0].url)
    }

    @Test
    fun `toUploadedFileUrls maps quarantined uploads to UploadedFileUrl with pending scan messageKey and no url`() {
        val uploads = listOf(quarantinedUpload("cert.pdf"))

        val result = uploads.toUploadedFileUrls("download.messageKey", mockUploadService)

        assertEquals(
            listOf(
                UploadedFileUrl(
                    messageKey = "propertyCompliance.uploadedFile.virusScanPendingWithName",
                    displayName = "cert.pdf",
                    url = null,
                ),
            ),
            result,
        )
    }

    @Test
    fun `toUploadedFileUrls filters out deleted uploads`() {
        val uploads =
            listOf(
                scannedUpload("scanned.pdf", "/download/scanned.pdf"),
                deletedUpload("deleted.pdf"),
            )

        val result = uploads.toUploadedFileUrls("download.messageKey", mockUploadService)

        assertEquals(1, result.size)
        assertEquals("scanned.pdf", result[0].displayName)
    }

    @Test
    fun `toUploadedFileUrls preserves input ordering across mixed status uploads`() {
        val uploads =
            listOf(
                scannedUpload("first.pdf", "/download/first.pdf"),
                quarantinedUpload("second.pdf"),
                scannedUpload("third.pdf", "/download/third.pdf"),
            )

        val result = uploads.toUploadedFileUrls("download.messageKey", mockUploadService)

        assertEquals(listOf("first.pdf", "second.pdf", "third.pdf"), result.map { it.displayName })
        assertEquals("/download/first.pdf", result[0].url)
        assertNull(result[1].url)
        assertEquals("/download/third.pdf", result[2].url)
    }

    @Test
    fun `toUploadedFileUrls returns empty list when given empty input`() {
        val result = emptyList<Pair<FileUpload, String>>().toUploadedFileUrls("download.messageKey", mockUploadService)

        assertEquals(emptyList<UploadedFileUrl>(), result)
    }
}
