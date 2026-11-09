package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.springframework.beans.MutablePropertyValues
import org.springframework.mock.web.MockHttpSession
import org.springframework.web.bind.WebDataBinder
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.journeys.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockFileItemInput
import java.io.File
import kotlin.reflect.KClass
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UploadCertificateFormModelTests {
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideUppercaseSupportedFileTypes")
    fun `isFileTypeValid accepts supported uppercase extensions`(
        fileName: String,
        contentType: String,
    ) {
        // Arrange
        val model =
            UploadCertificateFormModel().apply {
                name = fileName
                this.contentType = contentType
            }

        // Act
        val isValid = model.isFileTypeValid()

        // Assert
        assertTrue(isValid)
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = ["certificate.EXE", "certificate.DOCX", "bad_script.sh"])
    fun `isFileTypeValid rejects unsupported extensions`(fileName: String) {
        // Arrange
        val model =
            UploadCertificateFormModel().apply {
                name = fileName
                contentType = "application/pdf"
            }

        // Act
        val isValid = model.isFileTypeValid()

        // Assert
        assertFalse(isValid)
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = ["text/plain", "application/octet-stream"])
    fun `isFileTypeValid rejects invalid MIME types`(contentType: String) {
        // Arrange
        val model =
            UploadCertificateFormModel().apply {
                name = "certificate.pdf"
                this.contentType = contentType
            }

        // Act
        val isValid = model.isFileTypeValid()

        // Assert
        assertFalse(isValid)
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = ["certificate.PDF", "certificate.pdf"])
    fun `isContentLengthValid rejects oversized uploads`(fileName: String) {
        // Arrange
        val model =
            UploadCertificateFormModel().apply {
                name = fileName
                contentType = "application/pdf"
                contentLength = UploadCertificateFormModel.maxContentLength.toLong() + 1
            }

        // Act
        val isFileTypeValid = model.isFileTypeValid()
        val isContentLengthValid = model.isContentLengthValid()

        // Assert
        assertTrue(isFileTypeValid)
        assertFalse(isContentLengthValid)
    }

    @Test
    fun `fromUploadedFile returns a corresponding UploadCertificateFormModel`() {
        val fileName = "fileName"
        val contentType = "fileType"
        val contentLength = 20L
        val fileUpload = FileUpload()
        val fileItemInput = MockFileItemInput(name = fileName, contentType = contentType)

        val expectedModel =
            UploadCertificateFormModel().apply {
                this.name = fileName
                this.contentType = contentType
                this.contentLength = contentLength
                this.hasUploadFailed = false
                this.isUserSubmittedMetadataOnly = false
                this.fileUploadId = fileUpload.id
            }

        val returnedModel =
            UploadCertificateFormModel.fromUploadedFile(
                fileItemInput,
                contentLength,
                fileUpload.id,
            )

        assertTrue(ReflectionEquals(returnedModel).matches(expectedModel))
    }

    @ParameterizedTest(name = "for the {0} class")
    @MethodSource("provideUploadCertificateFormModelClasses")
    fun `UploadCertificateFormModel can be converted to the appropriate subclass by via session storage serialization`(
        desiredClass: KClass<out UploadCertificateFormModel>,
    ) {
        val fileName = "fileName"
        val contentType = "fileType"
        val contentLength = 20L
        val fileUpload = FileUpload()
        val fileItemInput = MockFileItemInput(name = fileName, contentType = contentType)

        val expectedModel =
            desiredClass.constructors.first().call().apply {
                this.name = fileName
                this.contentType = contentType
                this.contentLength = contentLength
                this.hasUploadFailed = false
                this.isUserSubmittedMetadataOnly = false
                this.fileUploadId = fileUpload.id
            }

        val baseModel =
            UploadCertificateFormModel.fromUploadedFile(
                fileItemInput,
                contentLength,
                fileUpload.id,
            )

        val session = MockHttpSession()
        session.setAttribute("test", baseModel.toPageData())
        val deserializedMap = objectToStringKeyedMap(session.getAttribute("test"))

        val binder = WebDataBinder(desiredClass.constructors.first().call())
        binder.bind(MutablePropertyValues(deserializedMap))
        binder.target

        assertTrue(ReflectionEquals(binder.bindingResult.target).matches(expectedModel))
    }

    @Test
    fun `maxFileSizeBytes in javascript matches max size in UploadCertificateFormModel`() {
        val constantsJsonString = File("src/main/js/fileUploadConstants.json").readText()
        val jsConstants = Json.decodeFromString<Map<String, Double>>(constantsJsonString)
        val jsMaxSizeMB = jsConstants["maxFileSizeBytes"]

        val ktMaxSizeMB = UploadCertificateFormModel.maxContentLength

        assertEquals(ktMaxSizeMB, jsMaxSizeMB, "File size limits must match between Kotlin and JavaScript")
    }

    companion object {
        @JvmStatic
        private fun provideUppercaseSupportedFileTypes() =
            listOf(
                Arguments.of("certificate.PDF", "application/pdf"),
                Arguments.of("certificate.PNG", "image/png"),
                Arguments.of("certificate.JPEG", "image/jpeg"),
                Arguments.of("certificate.JPG", "image/jpeg"),
            )

        @JvmStatic
        private fun provideUploadCertificateFormModelClasses() =
            listOf(
                Named.of(GasSafetyUploadCertificateFormModel::class.java.simpleName, GasSafetyUploadCertificateFormModel::class),
                Named.of(ElectricalUploadCertificateFormModel::class.java.simpleName, ElectricalUploadCertificateFormModel::class),
            )

        private class UnsupportedUploadCertificateFormModel(
            override val certificate: Nothing? = null,
        ) : UploadCertificateFormModel()
    }
}
