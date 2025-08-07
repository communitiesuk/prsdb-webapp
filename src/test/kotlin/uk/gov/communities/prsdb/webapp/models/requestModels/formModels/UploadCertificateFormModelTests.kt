package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.springframework.beans.MutablePropertyValues
import org.springframework.mock.web.MockHttpSession
import org.springframework.web.bind.WebDataBinder
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockFileItemInput
import java.io.File
import kotlin.reflect.KClass
import kotlin.test.assertTrue

class UploadCertificateFormModelTests {
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
