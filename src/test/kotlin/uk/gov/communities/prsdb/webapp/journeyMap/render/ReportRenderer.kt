package uk.gov.communities.prsdb.webapp.journeyMap.render

import uk.gov.communities.prsdb.webapp.journeyMap.model.JourneyMapManifest
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestNode

class ReportRenderer {
    fun render(manifest: JourneyMapManifest): String =
        buildString {
            val pages = manifest.nodes.filter { it.kind != ManifestNode.DECISION }
            appendLine("# ${manifest.journey.title} journey map report")
            appendLine()
            appendLine(ReadmeRenderer.summaryLine(manifest))
            appendLine()
            appendLine("| Pages | Variants | Edges | Gaps | Errors |")
            appendLine("|-------|----------|-------|------|--------|")
            appendLine(
                "| ${pages.size} | ${pages.sumOf { it.variants.size }} | ${manifest.edges.size} | " +
                    "${manifest.gaps.size} | ${manifest.errors.size} |",
            )
            appendLine()
            appendLine("## Gaps")
            appendLine()
            if (manifest.gaps.isEmpty()) {
                appendLine("None.")
            } else {
                appendLine("| Node | Mode | Reason |")
                appendLine("|------|------|--------|")
                manifest.gaps.forEach { appendLine("| ${cell(it.node)} | ${cell(it.mode ?: "")} | ${cell(it.reason)} |") }
            }
            appendLine()
            appendLine("## Errors")
            appendLine()
            if (manifest.errors.isEmpty()) {
                appendLine("None.")
            } else {
                appendLine("| Node | Kind | Detail |")
                appendLine("|------|------|--------|")
                manifest.errors.forEach { appendLine("| ${cell(it.node)} | ${cell(it.kind)} | ${cell(it.detail)} |") }
            }
            val capped = manifest.nodes.filter { it.skippedVariants > 0 }
            if (capped.isNotEmpty()) {
                appendLine()
                appendLine("## Variants not captured")
                appendLine()
                appendLine("These pages had more variants than the generator keeps for each page.")
                appendLine()
                appendLine("| Node | Variants not captured |")
                appendLine("|------|-----------------------|")
                capped.forEach { appendLine("| ${cell(it.id)} | ${it.skippedVariants} |") }
            }
            appendRoutingCode(manifest)
            if (manifest.dependencies.isNotEmpty()) {
                appendLine()
                appendLine("## Decisions and the answers they read")
                appendLine()
                appendLine("| Decision | Steps whose answers it reads | Reads every answer |")
                appendLine("|----------|------------------------------|--------------------|")
                manifest.dependencies.forEach {
                    val readsAll = if (it.readsAllAnswers) "yes" else "no"
                    appendLine("| ${cell(it.decision)} | ${cell(it.writers.joinToString(", "))} | $readsAll |")
                }
            }
        }

    private fun StringBuilder.appendRoutingCode(manifest: JourneyMapManifest) {
        if (manifest.arrows.isEmpty()) return
        val notWalked = manifest.arrows.filterNot { it.walked }
        val dependent = manifest.arrows.filter { it.reads.isNotEmpty() }
        appendLine()
        appendLine("## Routing code")
        appendLine()
        appendLine("Every arrow the journey's routing code defines between pages and decisions, for every outcome.")
        appendLine()
        appendLine("| Arrows | Walked | Not walked | Depend on earlier answers |")
        appendLine("|--------|--------|------------|---------------------------|")
        appendLine("| ${manifest.arrows.size} | ${manifest.arrows.size - notWalked.size} | ${notWalked.size} | ${dependent.size} |")
        if (notWalked.isNotEmpty()) {
            appendLine()
            appendLine("### Arrows not walked")
            appendLine()
            appendLine("| From | Outcome | To |")
            appendLine("|------|---------|----|")
            notWalked.forEach {
                appendLine("| ${cell(it.from)} | ${cell(it.mode)} | ${cell(it.to ?: "depends on earlier answers")} |")
            }
        }
        if (dependent.isNotEmpty()) {
            appendLine()
            appendLine("### Arrows that depend on earlier answers")
            appendLine()
            appendLine(
                "The routing code for these arrows also reads the journey state listed, so with other earlier answers they can " +
                    "lead elsewhere. Where they lead is only known once walked.",
            )
            appendLine()
            appendLine("| From | Outcome | Walked | Reads |")
            appendLine("|------|---------|--------|-------|")
            dependent.forEach {
                val walked = if (it.walked) "yes" else "no"
                appendLine("| ${cell(it.from)} | ${cell(it.mode)} | $walked | ${cell(it.reads.joinToString(", "))} |")
            }
        }
    }

    private fun cell(text: String) = text.replace("|", "\\|").replace("\n", " ")
}
