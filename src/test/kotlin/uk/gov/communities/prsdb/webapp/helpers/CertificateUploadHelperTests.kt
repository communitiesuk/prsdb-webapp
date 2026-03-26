package uk.gov.communities.prsdb.webapp.helpers

import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.validation.SimpleErrors
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService
import uk.gov.communities.prsdb.webapp.services.UploadService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockFileItemInput
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockFileItemInputIterator
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class CertificateUploadHelperTests {
    @Mock
    private lateinit var mockFileUploadCookieService: FileUploadCookieService

    @Mock
    private lateinit var mockUploadService: UploadService

    @Mock
    private lateinit var mockValidator: Validator

    @Mock
    private lateinit var mockRequest: HttpServletRequest

    @InjectMocks
    private lateinit var certificateUploadHelper: CertificateUploadHelper

    private val uploadFileName = "test-upload-file"
    private val token = "valid-token"

    private val validFileItemInput = MockFileItemInput(name = "test.png", contentType = "image/png")
    private val noValidationErrors = SimpleErrors(object {})
    private val validationErrors = SimpleErrors(object {}).apply { reject("any-error-code") }

    @Nested
    inner class UploadFileAndReturnFormModel {
        @Test
        fun `validates and uses the token`() {
            val fileInputIterator = MockFileItemInputIterator(listOf(validFileItemInput))
            whenever(mockRequest.contentLengthLong).thenReturn(100L)
            whenever(mockValidator.validateObject(any())).thenReturn(noValidationErrors)

            certificateUploadHelper.uploadFileAndReturnFormModel(uploadFileName, fileInputIterator, token, mockRequest)

            verify(mockFileUploadCookieService).validateAndUseToken(token)
        }

        @Test
        fun `throws ResponseStatusException when token validation fails`() {
            val fileInputIterator = MockFileItemInputIterator(listOf(validFileItemInput))
            whenever(mockFileUploadCookieService.validateAndUseToken(token))
                .thenThrow(ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid upload token"))

            assertThrows<ResponseStatusException> {
                certificateUploadHelper.uploadFileAndReturnFormModel(uploadFileName, fileInputIterator, token, mockRequest)
            }
        }

        @Test
        fun `throws ResponseStatusException when no file is present in the request`() {
            val emptyIterator = MockFileItemInputIterator(emptyList())

            val exception =
                assertThrows<ResponseStatusException> {
                    certificateUploadHelper.uploadFileAndReturnFormModel(uploadFileName, emptyIterator, token, mockRequest)
                }

            assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        }

        @Test
        fun `uploads the file when validation passes`() {
            val fileInputIterator = MockFileItemInputIterator(listOf(validFileItemInput))
            whenever(mockRequest.contentLengthLong).thenReturn(100L)
            whenever(mockValidator.validateObject(any())).thenReturn(noValidationErrors)
            whenever(mockUploadService.uploadFile(any(), any(), any())).thenReturn(FileUpload())

            certificateUploadHelper.uploadFileAndReturnFormModel(uploadFileName, fileInputIterator, token, mockRequest)

            verify(mockUploadService).uploadFile(any(), any(), any())
        }

        @Test
        fun `does not upload the file when validation fails`() {
            val fileInputIterator = MockFileItemInputIterator(listOf(validFileItemInput))
            whenever(mockRequest.contentLengthLong).thenReturn(100L)
            whenever(mockValidator.validateObject(any())).thenReturn(validationErrors)

            certificateUploadHelper.uploadFileAndReturnFormModel(uploadFileName, fileInputIterator, token, mockRequest)

            verify(mockUploadService, never()).uploadFile(any(), any(), any())
        }

        @Test
        fun `returns form data with fileUploadId when upload succeeds`() {
            val fileUpload = FileUpload()
            val fileInputIterator = MockFileItemInputIterator(listOf(validFileItemInput))
            whenever(mockRequest.contentLengthLong).thenReturn(100L)
            whenever(mockValidator.validateObject(any())).thenReturn(noValidationErrors)
            whenever(mockUploadService.uploadFile(any(), any(), any())).thenReturn(fileUpload)

            val formData =
                certificateUploadHelper.uploadFileAndReturnFormModel(uploadFileName, fileInputIterator, token, mockRequest)

            assertNotNull(formData["fileUploadId"])
        }

        @Test
        fun `returns form data without fileUploadId when upload returns null`() {
            val fileInputIterator = MockFileItemInputIterator(listOf(validFileItemInput))
            whenever(mockRequest.contentLengthLong).thenReturn(100L)
            whenever(mockValidator.validateObject(any())).thenReturn(noValidationErrors)
            whenever(mockUploadService.uploadFile(any(), any(), any())).thenReturn(null)

            val formData =
                certificateUploadHelper.uploadFileAndReturnFormModel(uploadFileName, fileInputIterator, token, mockRequest)

            assertNull(formData["fileUploadId"])
        }

        @Test
        fun `returns form data without fileUploadId when validation fails`() {
            val fileInputIterator = MockFileItemInputIterator(listOf(validFileItemInput))
            whenever(mockRequest.contentLengthLong).thenReturn(100L)
            whenever(mockValidator.validateObject(any())).thenReturn(validationErrors)

            val formData =
                certificateUploadHelper.uploadFileAndReturnFormModel(uploadFileName, fileInputIterator, token, mockRequest)

            assertNull(formData["fileUploadId"])
        }
    }

    @Nested
    inner class AddCookieIfStepIsFileUploadStep {
        @Test
        fun `delegates to FileUploadCookieService`() {
            val stepName = "any-step-name"

            certificateUploadHelper.addCookieIfStepIsFileUploadStep(stepName)

            verify(mockFileUploadCookieService).addCookieIfStepIsFileUploadStep(stepName)
        }
    }
}
