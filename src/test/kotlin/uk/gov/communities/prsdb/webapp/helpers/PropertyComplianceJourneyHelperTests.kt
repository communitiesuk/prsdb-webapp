package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import kotlin.test.assertEquals

class PropertyComplianceJourneyHelperTests {
    @ParameterizedTest(name = "for the {0} step")
    @MethodSource("provideFileUploadStepAndFileNames")
    fun `getCertFilename returns the corresponding file name`(stepName: String) {
        val fileName = PropertyComplianceJourneyHelper.getCertFilename(PROPERTY_OWNERSHIP_ID, stepName)

        val fileNameParts = fileName.split(".")
        val keyTypePart = fileNameParts[0]
        val propertyOwnershipIdPart = fileNameParts[1]
        val stepNamePart = fileNameParts[2]

        assertEquals(keyTypePart, "certificateUpload")
        assertEquals(propertyOwnershipIdPart, PROPERTY_OWNERSHIP_ID.toString())
        assertEquals(stepNamePart, stepName)
    }

    @ParameterizedTest(name = "for the {0} file category")
    @MethodSource("provideFileCategoryAndExpectedStepNames")
    fun `getCertFilename with FileCategory delegates to getCertFilename with the expected step name`(
        certificateType: CertificateType,
        expectedStepName: String,
    ) {
        val expectedFileName = PropertyComplianceJourneyHelper.getCertFilename(PROPERTY_OWNERSHIP_ID, expectedStepName)
        val actualFileName = PropertyComplianceJourneyHelper.getCertFilename(PROPERTY_OWNERSHIP_ID, certificateType)
        assertEquals(expectedFileName, actualFileName)
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 1L

        @JvmStatic
        private fun provideFileUploadStepAndFileNames() =
            arrayOf(
                Named.of(PropertyComplianceStepId.GasSafetyUpload.name, PropertyComplianceStepId.GasSafetyUpload.urlPathSegment),
                Named.of(PropertyComplianceStepId.EicrUpload.name, PropertyComplianceStepId.EicrUpload.urlPathSegment),
            )

        @JvmStatic
        private fun provideFileCategoryAndExpectedStepNames() =
            arrayOf(
                Arguments.of(
                    Named.of(CertificateType.GasSafetyCert.name, CertificateType.GasSafetyCert),
                    PropertyComplianceStepId.GasSafetyUpload.urlPathSegment,
                ),
                Arguments.of(
                    Named.of(CertificateType.Eicr.name, CertificateType.Eicr),
                    PropertyComplianceStepId.EicrUpload.urlPathSegment,
                ),
            )
    }
}
