package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import org.junit.jupiter.api.Test
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockFileItemInput
import kotlin.test.assertTrue

class UploadCertificateFormModelTests {
    @Test
    fun `fromFileItemInput returns a corresponding UploadCertificateFormModel`() {
        val fileName = "fileName"
        val contentType = "fileType"
        val contentLength = 20L
        val isUploadSuccessful = true
        val fileItemInput = MockFileItemInput(name = fileName, contentType = contentType)

        val expectedModel =
            UploadCertificateFormModel().apply {
                this.name = fileName
                this.contentType = contentType
                this.contentLength = contentLength
                this.isUploadSuccessful = isUploadSuccessful
            }

        val returnedModel = UploadCertificateFormModel.fromFileItemInput(fileItemInput, contentLength, isUploadSuccessful)

        assertTrue(ReflectionEquals(returnedModel).matches(expectedModel))
    }
}
