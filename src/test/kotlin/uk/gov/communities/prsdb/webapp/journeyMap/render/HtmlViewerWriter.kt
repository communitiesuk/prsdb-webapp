package uk.gov.communities.prsdb.webapp.journeyMap.render

import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.journeyMap.model.JourneyMapManifest

class HtmlViewerWriter(
    private val template: String = loadTemplate(),
) {
    fun render(
        manifest: JourneyMapManifest,
        svg: String?,
    ): String {
        val manifestJson = json.encodeToString(manifest).replace("</", "<\\/")
        return template
            .replace("__TITLE__", escapeHtml("${manifest.journey.title} journey map"))
            .replace("/*__MANIFEST__*/null", manifestJson)
            .replace("<!--__SVG__-->", svg ?: "")
    }

    companion object {
        private val json = Json { encodeDefaults = true }

        private fun loadTemplate(): String =
            HtmlViewerWriter::class.java.classLoader
                .getResource("journey-map/viewer.html")
                ?.readText()
                ?: throw IllegalStateException("journey-map/viewer.html is missing from the test resources")

        private fun escapeHtml(text: String) =
            text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
    }
}
