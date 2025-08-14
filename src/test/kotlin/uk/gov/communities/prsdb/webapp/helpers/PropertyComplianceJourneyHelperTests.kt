package uk.gov.communities.prsdb.webapp.helpers

import kotlinx.datetime.Clock
import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import kotlin.test.assertEquals

class PropertyComplianceJourneyHelperTests {
    @ParameterizedTest(name = "for the {0} step")
    @MethodSource("provideFileUploadStepAndFileNames")
    fun `getCertFilename returns the corresponding file name`(stepName: String) {
        val timeBefore = Clock.System.now()
        val fileName = PropertyComplianceJourneyHelper.getCertFilename(PROPERTY_OWNERSHIP_ID, stepName)

        val fileNameParts = fileName.split(".")
        val keyTypePart = fileNameParts[0]
        val propertyOwnershipIdPart = fileNameParts[1]
        val stepNamePart = fileNameParts[2]

        assertEquals(
            keyTypePart,
            "certificateUpload",
        )
        assertEquals(
            propertyOwnershipIdPart,
            PROPERTY_OWNERSHIP_ID.toString(),
        )
        assertEquals(
            stepNamePart,
            stepName,
        )
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 1L

        @JvmStatic
        private fun provideFileUploadStepAndFileNames() =
            arrayOf(
                Named.of(PropertyComplianceStepId.GasSafetyUpload.name, PropertyComplianceStepId.GasSafetyUpload.urlPathSegment),
                Named.of(PropertyComplianceStepId.EicrUpload.name, PropertyComplianceStepId.EicrUpload.urlPathSegment),
            )
    }
}
