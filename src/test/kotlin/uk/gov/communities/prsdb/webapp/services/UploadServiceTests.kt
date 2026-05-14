package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.UploadedFileLocator
import java.io.InputStream
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class UploadServiceTests {
    @Mock
    private lateinit var mockUploader: FileUploader

    @Mock
    private lateinit var mockRepository: FileUploadRepository

    @Mock
    private lateinit var mockDownloader: FileDownloader

    @Mock
    private lateinit var mockSafeFileDeleter: SafeFileDeleter

    @InjectMocks
    private lateinit var uploadService: UploadService

    @Test
    fun `uploadFile uploads the input stream to a file and saves the result as quarantined`() {
        // Arrange
        val proposedObjectKey = "testObjectKey"
        val mockUploadResult = UploadedFileLocator(proposedObjectKey, "mockETag", "mockVersionId")

        whenever(mockUploader.uploadFile(any(), any()))
            .thenReturn(mockUploadResult)

        whenever(mockRepository.save(any()))
            .thenAnswer { invocation -> invocation.getArgument<FileUpload>(0) }

        // Act
        val result = uploadService.uploadFile(proposedObjectKey, InputStream.nullInputStream(), "txt")

        // Assert
        verify(mockRepository).save(eq(result!!))
        assertEquals(proposedObjectKey, result.objectKey)
        assertEquals(mockUploadResult.eTag, result.eTag)
        assertEquals(mockUploadResult.versionId, result.versionId)
        assertEquals(FileUploadStatus.QUARANTINED, result.status)
    }

    @Test
    fun `uploadFile stores the fileName when one is provided`() {
        // Arrange
        val proposedObjectKey = "testObjectKey"
        val mockUploadResult = UploadedFileLocator(proposedObjectKey, "mockETag", "mockVersionId")
        val fileName = "testDocument.pdf"

        whenever(mockUploader.uploadFile(any(), any()))
            .thenReturn(mockUploadResult)

        whenever(mockRepository.save(any()))
            .thenAnswer { invocation -> invocation.getArgument<FileUpload>(0) }

        // Act
        val result = uploadService.uploadFile(proposedObjectKey, InputStream.nullInputStream(), "pdf", fileName)

        // Assert
        assertEquals(fileName, result!!.fileName)
    }

    @Test
    fun `when uploading fails, uploadFile does not save a result and returns null`() {
        // Given
        val proposedObjectKey = "testObjectKey"
        whenever(mockUploader.uploadFile(any(), any()))
            .thenReturn(null)

        // When
        val result = uploadService.uploadFile(proposedObjectKey, InputStream.nullInputStream(), "txt")

        // Then
        verify(mockRepository, never()).save(any())
        assertNull(result)
    }

    @Test
    fun `deleteUploadedFile sets status to DELETED and deletes from safe bucket for scanned files`() {
        val fileUpload = FileUpload(FileUploadStatus.SCANNED, "key", "txt", "eTag", "versionId")
        whenever(mockRepository.findById(1L)).thenReturn(Optional.of(fileUpload))
        whenever(mockSafeFileDeleter.deleteFile(any())).thenReturn(true)

        uploadService.deleteUploadedFile(1L)

        assertEquals(FileUploadStatus.DELETED, fileUpload.status)
        verify(mockRepository).save(fileUpload)
        verify(mockSafeFileDeleter).deleteFile(fileUpload)
    }

    @Test
    fun `deleteUploadedFile sets status to DELETED without S3 deletion for quarantined files`() {
        val fileUpload = FileUpload(FileUploadStatus.QUARANTINED, "key", "txt", "eTag", "versionId")
        whenever(mockRepository.findById(1L)).thenReturn(Optional.of(fileUpload))

        uploadService.deleteUploadedFile(1L)

        assertEquals(FileUploadStatus.DELETED, fileUpload.status)
        verify(mockRepository).save(fileUpload)
        verify(mockSafeFileDeleter, never()).deleteFile(any())
    }

    @Test
    fun `deleteUploadedFile is a no-op for already deleted files`() {
        val fileUpload = FileUpload(FileUploadStatus.DELETED, "key", "txt", "eTag", "versionId")
        whenever(mockRepository.findById(1L)).thenReturn(Optional.of(fileUpload))

        uploadService.deleteUploadedFile(1L)

        verify(mockRepository, never()).save(any())
        verify(mockSafeFileDeleter, never()).deleteFile(any())
    }
}
