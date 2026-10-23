package uk.gov.communities.prsdb.webapp.journeyMap.render

import uk.gov.communities.prsdb.webapp.journeyMap.model.JourneyMapManifest
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestNode

class ReadmeRenderer(
    private val mermaidRenderer: MermaidRenderer = MermaidRenderer(),
) {
    fun render(manifest: JourneyMapManifest): String =
        buildString {
            appendLine("# ${manifest.journey.title} journey map")
            appendLine()
            appendLine(summaryLine(manifest))
            appendLine()
            appendLine("Open `index.html` for the interactive map. `report.md` lists gaps and errors.")
            appendLine()
            appendLine("```mermaid")
            append(mermaidRenderer.render(manifest))
            appendLine("```")
            appendLine()
            appendLine("## Pages")
            manifest.nodes
                .filter { it.kind != ManifestNode.DECISION }
                .forEach { node ->
                    appendLine()
                    appendLine("### ${node.title}")
                    appendLine()
                    appendLine(listOfNotNull(node.route?.let { "`$it`" }, node.stepConfig).joinToString(" · "))
                    appendLine()
                    if (node.variants.isEmpty()) {
                        appendLine("_Not reached by the generator._")
                    }
                    node.variants.forEach { variant ->
                        val conditions =
                            if (variant.conditions.isEmpty()) {
                                "Shown on the default route"
                            } else {
                                "Shown when " + variant.conditions.joinToString("; ") { "${it.step}: ${it.answer}" }
                            }
                        val origin = if (variant.origin == ManifestNode.ORIGIN_CHANGE) " (when changing an answer)" else ""
                        appendLine("[![${variant.id}](${variant.thumbnail})](${variant.image})")
                        appendLine()
                        appendLine("$conditions$origin")
                        appendLine()
                    }
                }
        }

    companion object {
        fun summaryLine(manifest: JourneyMapManifest): String {
            val flags = manifest.journey.featureFlags.entries.joinToString(", ") { "${it.key}=${it.value}" }
            val commit = manifest.journey.gitCommit?.let { " at commit `$it`" } ?: ""
            return "Generated ${manifest.journey.generatedAt}$commit. Feature flags: $flags."
        }
    }
}
