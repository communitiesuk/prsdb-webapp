package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.UploadedFileLocator

@ExtendWith(MockitoExtension::class)
class UploadDequarantinerTests {
    @Mock
    private lateinit var dequarantiningFileCopier: DequarantiningFileCopier

    @Mock
    private lateinit var quarantinedFileDeleter: QuarantinedFileDeleter

    @Mock
    private lateinit var fileUploadRepository: FileUploadRepository

    @InjectMocks
    private lateinit var uploadDequarantiner: UploadDequarantiner

    @Test
    fun `when a file is copied to a safe area and the original deleted, dequarantineFile returns true and the new details are saved`() {
        // Arrange
        val fileUpload =
            FileUpload(
                status = FileUploadStatus.QUARANTINED,
                objectKey = "s3Key",
                extension = "txt",
                eTag = "eTag",
                versionId = "versionId",
            )

        val transferResult =
            UploadedFileLocator(
                objectKey = fileUpload.objectKey,
                eTag = "newETag",
                versionId = "newVersionId",
            )

        whenever(dequarantiningFileCopier.copyFile(fileUpload)).thenReturn(transferResult)
        whenever(quarantinedFileDeleter.deleteFile(fileUpload)).thenReturn(true)

        // Act
        val result = uploadDequarantiner.dequarantineFile(fileUpload)

        // Assert
        verify(fileUploadRepository).save(fileUpload)
        assertEquals(FileUploadStatus.SCANNED, fileUpload.status)
        assertEquals(transferResult.eTag, fileUpload.eTag)
        assertEquals(transferResult.versionId, fileUpload.versionId)
        assertEquals(transferResult.objectKey, fileUpload.objectKey)
        assertEquals("txt", fileUpload.extension)
        assertTrue(result)
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun `when a file cannot be copied to a safe area, dequarantineFile returns false, the file is not deleted and no new details are saved`() {
        // Arrange
        val originalETag = "eTag"
        val fileUpload =
            FileUpload(
                status = FileUploadStatus.QUARANTINED,
                objectKey = "s3Key",
                extension = "txt",
                eTag = originalETag,
                versionId = "versionId",
            )

        whenever(dequarantiningFileCopier.copyFile(fileUpload)).thenReturn(null)

        // Act
        val result = uploadDequarantiner.dequarantineFile(fileUpload)

        // Assert
        verify(quarantinedFileDeleter, never()).deleteFile(any())
        verify(fileUploadRepository, never()).save(any())
        assertFalse(result)
        assertEquals(originalETag, fileUpload.eTag)
    }

    @Test
    fun `when a file cannot be deleted, dequarantineFile returns false and no new details are saved`() {
        // Arrange
        val originalETag = "eTag"
        val fileUpload =
            FileUpload(
                status = FileUploadStatus.QUARANTINED,
                objectKey = "s3Key",
                extension = "txt",
                eTag = originalETag,
                versionId = "versionId",
            )

        val transferResult =
            UploadedFileLocator(
                objectKey = fileUpload.objectKey,
                eTag = "newETag",
                versionId = "newVersionId",
            )

        whenever(dequarantiningFileCopier.copyFile(fileUpload)).thenReturn(transferResult)
        whenever(quarantinedFileDeleter.deleteFile(fileUpload)).thenReturn(false)

        // Act
        val result = uploadDequarantiner.dequarantineFile(fileUpload)

        // Assert
        verify(fileUploadRepository, never()).save(any())
        assertFalse(result)
        assertEquals(originalETag, fileUpload.eTag)
    }

    @Test
    fun `when a file is deleted, deleteQuarantinedFile returns true and the new details are saved`() {
        // Arrange
        val originalETag = "eTag"
        val fileUpload =
            FileUpload(
                status = FileUploadStatus.QUARANTINED,
                objectKey = "s3Key",
                extension = "txt",
                eTag = originalETag,
                versionId = "versionId",
            )

        whenever(quarantinedFileDeleter.deleteFile(fileUpload)).thenReturn(true)

        // Act
        val result = uploadDequarantiner.deleteQuarantinedFile(fileUpload)

        // Assert
        verify(fileUploadRepository).save(fileUpload)
        assertEquals(FileUploadStatus.DELETED, fileUpload.status)
        assertEquals(originalETag, fileUpload.eTag)
        assertTrue(result)
    }

    @Test
    fun `when a file cannot be deleted, deleteQuarantinedFile returns false and no new details are saved`() {
        // Arrange
        val originalETag = "eTag"
        val fileUpload =
            FileUpload(
                status = FileUploadStatus.QUARANTINED,
                objectKey = "s3Key",
                extension = "txt",
                eTag = originalETag,
                versionId = "versionId",
            )

        whenever(quarantinedFileDeleter.deleteFile(fileUpload)).thenReturn(false)

        // Act
        val result = uploadDequarantiner.deleteQuarantinedFile(fileUpload)

        // Assert
        verify(fileUploadRepository, never()).save(any())
        assertFalse(result)
        assertEquals(originalETag, fileUpload.eTag)
    }
}
