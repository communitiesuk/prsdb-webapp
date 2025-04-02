package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PropertyDetailsEmailSectionTests {
    @Test
    fun `PropertyDetailsEmailSection toString returns a template string with the details of a single property`() {
        val expectedMarkdown =
            "### Property 1 \n\n" +
                "Property registration number: \n\n" +
                "^P-WWW-XXX \n\n" +
                "Address: 1 Imaginary Street, Fakeville, FA1 2KE \n\n"

        val actualMarkdown =
            PropertyDetailsEmailSection(
                1,
                "P-WWW-XXX",
                "1 Imaginary Street, Fakeville, FA1 2KE",
            ).toString()

        assertEquals(expectedMarkdown, actualMarkdown)
    }

    @Test
    fun `PropertyDetailsEmailSectionList toString returns a template string with details of all the properties`() {
        val expectedMarkdown =
            "### Property 1 \n\n" +
                "Property registration number: \n\n" +
                "^P-WWW-XXX \n\n" +
                "Address: 1 Imaginary Street, Fakeville, FA1 2KE \n\n" +
                "--- \n" +
                "### Property 2 \n\n" +
                "Property registration number: \n\n" +
                "^P-YYY-ZZZ \n\n" +
                "Address: 2 Mythical Place, Fakeville, FA3 4KE \n\n"

        val actualMarkdown =
            PropertyDetailsEmailSectionList(
                listOf(
                    PropertyDetailsEmailSection(
                        1,
                        "P-WWW-XXX",
                        "1 Imaginary Street, Fakeville, FA1 2KE",
                    ),
                    PropertyDetailsEmailSection(
                        2,
                        "P-YYY-ZZZ",
                        "2 Mythical Place, Fakeville, FA3 4KE",
                    ),
                ),
            ).toString()

        assertEquals(expectedMarkdown, actualMarkdown)
    }
}
