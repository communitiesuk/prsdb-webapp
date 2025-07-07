package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.FieldSource
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException

class FileNameInfoTests {
    companion object {
        @JvmStatic
        val validFilenames =
            listOf(
                Arguments.of(
                    "property_123_eicr.pdf",
                    PropertyFileNameInfo(
                        propertyOwnershipId = 123L,
                        fileCategory = PropertyFileNameInfo.FileCategory.Eirc,
                        extension = "pdf",
                    ),
                ),
                Arguments.of(
                    "ignored_456_gas_safety_certificate.jpg",
                    PropertyFileNameInfo(
                        propertyOwnershipId = 456L,
                        fileCategory = PropertyFileNameInfo.FileCategory.GasSafetyCert,
                        extension = "jpg",
                    ),
                ),
            )

        @JvmStatic
        val invalidFilenames =
            listOf(
                Named.named("Missing extension", "property_123_eicr"),
                Named.named("Has multiple extensions", "property_123_eicr.pdf.txt"),
                Named.named("Too few name sections", "property_123.pdf"),
                Named.named("Invalid property ownership ID", "property_abc_eicr.pdf"),
                Named.named("Invalid file category", "property_123_invalid_category.pdf"),
                Named.named("Missing first section", "123_gas_safety_certificate.pdf"),
            )
    }

    @ParameterizedTest
    @FieldSource("validFilenames")
    fun `correctly parses valid filenames`(
        filename: String,
        expected: PropertyFileNameInfo,
    ) {
        val fileNameInfo = PropertyFileNameInfo.Companion.parse(filename)

        Assertions.assertEquals(expected, fileNameInfo)
    }

    @ParameterizedTest
    @FieldSource("invalidFilenames")
    fun `throws for invalid filenames`(filename: String) {
        assertThrows<PrsdbWebException> { PropertyFileNameInfo.Companion.parse(filename) }
    }
}
