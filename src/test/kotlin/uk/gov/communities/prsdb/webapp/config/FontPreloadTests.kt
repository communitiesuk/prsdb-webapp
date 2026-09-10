package uk.gov.communities.prsdb.webapp.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

// If these fail after a govuk-frontend upgrade, the font filename hashes have changed. Fix by updating
// the preload hrefs in layout.html to match the filenames the new govuk-frontend css requests.
class FontPreloadTests {
    @Test
    fun `preloaded fonts match the woff2 files requested by govuk-frontend css`() {
        val cssFontFileNames = cssFontFileNames()

        assertTrue(cssFontFileNames.isNotEmpty(), "No woff2 URLs were found in $GOVUK_FRONTEND_CSS")
        assertEquals(
            cssFontFileNames,
            preloadedFontFileNames(),
            "The preload hrefs in $LAYOUT_TEMPLATE no longer match the fonts requested by $GOVUK_FRONTEND_CSS. " +
                "Update them to the expected filenames below.",
        )
    }

    @Test
    fun `every preloaded font is present in the built assets`() {
        preloadedFontFileNames().forEach { fileName ->
            assertTrue(
                javaClass.getResource("$FONT_DIRECTORY/$fileName") != null,
                "Preloaded font $fileName is missing from $FONT_DIRECTORY. " +
                    "Update the preload hrefs in $LAYOUT_TEMPLATE to a font that govuk-frontend still ships.",
            )
        }
    }

    private fun preloadedFontFileNames() =
        PRELOAD_LINK_REGEX
            .findAll(readClasspathFile(LAYOUT_TEMPLATE))
            .map { it.groupValues[1] }
            .toSet()

    private fun cssFontFileNames() =
        FONT_URL_REGEX
            .findAll(readClasspathFile(GOVUK_FRONTEND_CSS))
            .map { it.groupValues[1] }
            .toSet()

    private fun readClasspathFile(path: String) =
        javaClass.getResource(path)?.readText() ?: throw IllegalStateException("$path was not found on the classpath")

    companion object {
        private const val LAYOUT_TEMPLATE = "/templates/fragments/layout.html"
        private const val GOVUK_FRONTEND_CSS = "/static/assets/css/govuk-frontend.min.css"
        private const val FONT_DIRECTORY = "/static/assets/fonts"

        private val PRELOAD_LINK_REGEX = Regex("""rel="preload"[^>]*/assets/fonts/([^}"]+\.woff2)""")
        private val FONT_URL_REGEX = Regex("""/assets/fonts/([\w-]+\.woff2)""")
    }
}
