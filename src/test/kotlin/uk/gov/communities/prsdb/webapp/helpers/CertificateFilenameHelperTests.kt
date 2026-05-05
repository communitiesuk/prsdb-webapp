package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import kotlin.test.assertEquals

class CertificateFilenameHelperTests {
    @ParameterizedTest(name = "for the {0} step")
    @MethodSource("provideFileUploadStepAndFileNames")
    fun `getCertFilename returns the corresponding file name`(stepName: String) {
        val fileName = CertificateFilenameHelper.getCertFilename(PROPERTY_OWNERSHIP_ID, stepName)

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
        val expectedFileName = CertificateFilenameHelper.getCertFilename(PROPERTY_OWNERSHIP_ID, expectedStepName)
        val actualFileName = CertificateFilenameHelper.getCertFilename(PROPERTY_OWNERSHIP_ID, certificateType)
        assertEquals(expectedFileName, actualFileName)
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 1L
        private const val GAS_SAFETY_UPLOAD_ROUTE_SEGMENT = "gas-safety-certificate-file-upload"
        private const val EICR_UPLOAD_ROUTE_SEGMENT = "eicr-file-upload"

        @JvmStatic
        private fun provideFileUploadStepAndFileNames() =
            arrayOf(
                Named.of("GasSafetyUpload", GAS_SAFETY_UPLOAD_ROUTE_SEGMENT),
                Named.of("EicrUpload", EICR_UPLOAD_ROUTE_SEGMENT),
            )

        @JvmStatic
        private fun provideFileCategoryAndExpectedStepNames() =
            arrayOf(
                Arguments.of(
                    Named.of(CertificateType.GasSafetyCert.name, CertificateType.GasSafetyCert),
                    GAS_SAFETY_UPLOAD_ROUTE_SEGMENT,
                ),
                Arguments.of(
                    Named.of(CertificateType.Eicr.name, CertificateType.Eicr),
                    EICR_UPLOAD_ROUTE_SEGMENT,
                ),
            )
    }
}
