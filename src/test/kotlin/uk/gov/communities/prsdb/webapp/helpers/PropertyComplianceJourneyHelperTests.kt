package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UploadCertificateFormModel
import kotlin.reflect.KClass
import kotlin.test.assertEquals

class PropertyComplianceJourneyHelperTests {
    @ParameterizedTest(name = "for the {0} step")
    @MethodSource("provideFileUploadStepNamesAndFormModelClasses")
    fun `getUploadCertificateFormModelClass returns the corresponding form model`(
        stepName: String,
        expectedFormModel: KClass<out UploadCertificateFormModel>,
    ) {
        assertEquals(
            expectedFormModel,
            PropertyComplianceJourneyHelper.getUploadCertificateFormModelClass(stepName),
        )
    }

    @Test
    fun `getUploadCertificateFormModelClass throws an IllegalStateException for invalid file upload step names`() {
        val invalidStepName = "invalid-step"

        assertThrows<IllegalStateException> {
            PropertyComplianceJourneyHelper.getUploadCertificateFormModelClass(invalidStepName)
        }
    }

    @ParameterizedTest(name = "for the {0} step")
    @MethodSource("provideFileUploadStepAndFileNames")
    fun `getCertFilename returns the corresponding file name`(
        stepName: String,
        expectedFileName: String,
    ) {
        val originalFileName = "any-name.$ORIGINAL_FILE_EXT"

        assertEquals(
            expectedFileName,
            PropertyComplianceJourneyHelper.getCertFilename(PROPERTY_OWNERSHIP_ID, stepName, originalFileName),
        )
    }

    @Test
    fun `getCertFilename throws an IllegalStateException for invalid file upload step names`() {
        val invalidStepName = "invalid-step"
        val originalFileName = "any-name.$ORIGINAL_FILE_EXT"

        assertThrows<IllegalStateException> {
            PropertyComplianceJourneyHelper.getCertFilename(PROPERTY_OWNERSHIP_ID, invalidStepName, originalFileName)
        }
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 1L
        private const val ORIGINAL_FILE_EXT = "png"

        @JvmStatic
        private fun provideFileUploadStepNamesAndFormModelClasses() =
            arrayOf(
                Arguments.of(
                    Named.of(PropertyComplianceStepId.GasSafetyUpload.name, PropertyComplianceStepId.GasSafetyUpload.urlPathSegment),
                    GasSafetyUploadCertificateFormModel::class,
                ),
                Arguments.of(
                    Named.of(PropertyComplianceStepId.EicrUpload.name, PropertyComplianceStepId.EicrUpload.urlPathSegment),
                    EicrUploadCertificateFormModel::class,
                ),
            )

        @JvmStatic
        private fun provideFileUploadStepAndFileNames() =
            arrayOf(
                Arguments.of(
                    Named.of(PropertyComplianceStepId.GasSafetyUpload.name, PropertyComplianceStepId.GasSafetyUpload.urlPathSegment),
                    "property_${PROPERTY_OWNERSHIP_ID}_gas_safety_certificate.$ORIGINAL_FILE_EXT",
                ),
                Arguments.of(
                    Named.of(PropertyComplianceStepId.EicrUpload.name, PropertyComplianceStepId.EicrUpload.urlPathSegment),
                    "property_${PROPERTY_OWNERSHIP_ID}_eicr.$ORIGINAL_FILE_EXT",
                ),
            )
    }
}
