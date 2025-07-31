package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockFileItemInput
import java.io.File
import kotlin.reflect.KClass
import kotlin.test.assertTrue

class UploadCertificateFormModelTests {
    @ParameterizedTest
    @MethodSource("provideUploadCertificateFormModelClasses")
    fun `fromFileItemInput returns a corresponding UploadCertificateFormModel`(desiredClass: KClass<out UploadCertificateFormModel>) {
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
                this.isUploadSuccessfulOrNull = true
                this.isUserSubmittedMetadataOnly = false
                this.fileUploadId = fileUpload.id
            }

        val returnedModel =
            UploadCertificateFormModel.fromFileItemUpload(
                desiredClass,
                fileItemInput,
                contentLength,
                fileUpload.id,
            )

        assertTrue(ReflectionEquals(returnedModel).matches(expectedModel))
    }

    @Test
    fun `fromFileInput throws an IllegalStateException when the desired class is not supported`() {
        val fileName = "fileName"
        val contentType = "fileType"
        val contentLength = 20L
        val fileUpload = FileUpload()
        val fileItemInput = MockFileItemInput(name = fileName, contentType = contentType)

        assertThrows<IllegalStateException> {
            UploadCertificateFormModel.fromFileItemUpload(
                UnsupportedUploadCertificateFormModel::class,
                fileItemInput,
                contentLength,
                fileUpload.id,
            )
        }
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
        private fun provideUploadCertificateFormModelClasses() =
            listOf(
                Named.of(GasSafetyUploadCertificateFormModel::class.simpleName, GasSafetyUploadCertificateFormModel::class),
                Named.of(EicrUploadCertificateFormModel::class.simpleName, EicrUploadCertificateFormModel::class),
            )

        private class UnsupportedUploadCertificateFormModel(
            override val certificate: Nothing? = null,
        ) : UploadCertificateFormModel()
    }
}
