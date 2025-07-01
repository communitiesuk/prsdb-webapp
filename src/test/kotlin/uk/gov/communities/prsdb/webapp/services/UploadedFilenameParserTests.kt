package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.FieldSource
import org.mockito.Mockito.times
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.services.UploadedFilenameParser.Companion.FileCategory
import uk.gov.communities.prsdb.webapp.services.UploadedFilenameParser.Companion.FileNameInfo

class UploadedFilenameParserTests {
    companion object {
        @JvmStatic
        val validFilenames =
            listOf(
                Arguments.of(
                    "property_123_eicr.pdf",
                    FileNameInfo(
                        propertyOwnershipId = 123L,
                        fileCategory = FileCategory.Eirc,
                        extension = "pdf",
                    ),
                ),
                Arguments.of(
                    "ignored_456_gas_safety_certificate.jpg",
                    FileNameInfo(
                        propertyOwnershipId = 456L,
                        fileCategory = FileCategory.GasSafetyCert,
                        extension = "jpg",
                    ),
                ),
            )

        @JvmStatic
        val invalidFilenames =
            listOf(
                named("Missing extension", "property_123_eicr"),
                named("Has multiple extensions", "property_123_eicr.pdf.txt"),
                named("Too few name sections", "property_123.pdf"),
                named("Invalid property ownership ID", "property_abc_eicr.pdf"),
                named("Invalid file category", "property_123_invalid_category.pdf"),
                named("Missing first section", "123_gas_safety_certificate.pdf"),
            )
    }

    @ParameterizedTest
    @FieldSource("validFilenames")
    fun `correctly parses valid filenames`(
        filename: String,
        expected: FileNameInfo,
    ) {
        val fileNameInfo = UploadedFilenameParser.parse(filename)

        assertEquals(expected, fileNameInfo)
    }

    @ParameterizedTest
    @FieldSource("invalidFilenames")
    fun `throws for invalid filenames`(filename: String) {
        assertThrows<PrsdbWebException> { UploadedFilenameParser.parse(filename) }
    }
}
