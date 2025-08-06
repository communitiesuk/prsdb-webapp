package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.FileUploadResult
import java.io.InputStream

@ExtendWith(MockitoExtension::class)
class UploadServiceTests {
    @Mock
    private lateinit var mockUploader: FileUploader

    @Mock
    private lateinit var mockRepository: FileUploadRepository

    @InjectMocks
    private lateinit var uploadService: UploadService

    @Test
    fun `uploadFile uploads the input stream to a file and saves the result as quarantined`() {
        // Arrange
        val proposedObjectKey = "testObjectKey"
        val mockUploadResult = FileUploadResult(proposedObjectKey, "mockETag", "mockVersionId")

        whenever(mockUploader.uploadFile(any(), any()))
            .thenReturn(mockUploadResult)

        whenever(mockRepository.save(any()))
            .thenAnswer { invocation -> invocation.getArgument<FileUpload>(0) }

        // Act
        val result = uploadService.uploadFile(proposedObjectKey, InputStream.nullInputStream(), "txt")

        // Assert
        assertEquals(proposedObjectKey, result?.objectKey)
        assertEquals(mockUploadResult.eTag, result?.eTag)
        assertEquals(mockUploadResult.versionId, result?.versionId)
        assertEquals(FileUploadStatus.QUARANTINED, result?.status)
    }

    @Test
    fun `when uploading fails, uploadFile does not save a result and returns null`() {
        // Given
        val mockUploader = mock<FileUploader>()
        val mockRepository = mock<FileUploadRepository>()
        val uploadService = UploadService(mockUploader, mockRepository)

        val proposedObjectKey = "testObjectKey"
        whenever(mockUploader.uploadFile(any(), any()))
            .thenReturn(null)

        // When
        val result = uploadService.uploadFile(proposedObjectKey, InputStream.nullInputStream(), "txt")

        // Then
        verify(mockRepository, never()).save(any())
        assertNull(result)
    }
}
