package uk.gov.communities.prsdb.webapp.journeyMap.render

import uk.gov.communities.prsdb.webapp.journeyMap.model.JourneyMapManifest
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestContainer
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestEdge
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestNode

class MermaidRenderer {
    fun render(
        manifest: JourneyMapManifest,
        htmlLabels: Boolean = false,
        thumbnailWidth: Int = 180,
        edgeIds: Boolean = false,
    ): String {
        val nodeIds = manifest.nodes.mapIndexed { index, node -> node.id to "n$index" }.toMap()
        val containerIds = manifest.containers.mapIndexed { index, container -> container.id to "c$index" }.toMap()
        val drawnContainers = drawnContainers(manifest)
        val containersById = manifest.containers.associateBy { it.id }

        fun drawnParent(container: ManifestContainer): String? {
            var parent = container.parent
            while (parent != null && parent !in drawnContainers) parent = containersById[parent]?.parent
            return parent
        }

        val childContainers = manifest.containers.filter { it.id in drawnContainers }.groupBy { drawnParent(it) }
        val nodesByContainer = manifest.nodes.groupBy { node -> node.containers.lastOrNull { it in drawnContainers } }

        val lines = mutableListOf("flowchart TD")
        // The start is declared first, so that the layout keeps it above every page
        if (manifest.entries.isNotEmpty()) lines += "    $START_ID((\"Start\"))"

        fun emitNodes(
            containerId: String?,
            indent: String,
        ) {
            nodesByContainer[containerId].orEmpty().forEach { node ->
                lines += indent + nodeIds.getValue(node.id) + shape(node, label(node, htmlLabels, thumbnailWidth))
            }
        }

        fun emitContainer(
            container: ManifestContainer,
            indent: String,
        ) {
            lines += "${indent}subgraph ${containerIds.getValue(container.id)}[\"${escape(container.label)}\"]"
            childContainers[container.id].orEmpty().forEach { emitContainer(it, "$indent    ") }
            emitNodes(container.id, "$indent    ")
            lines += "${indent}end"
        }

        childContainers[null].orEmpty().forEach { emitContainer(it, "    ") }
        emitNodes(null, "    ")

        manifest.entries.forEachIndexed { index, entry ->
            val to = nodeIds[entry.node] ?: return@forEachIndexed
            val id = if (edgeIds) "s$index@" else ""
            lines += "    $START_ID $id-->${labelPart(entry.label)} $to"
        }
        val changeOnlyNodes = manifest.nodes.filter { it.origin == ManifestNode.ORIGIN_CHANGE }.map { it.id }.toSet()
        manifest.edges.forEachIndexed { index, edge ->
            val drawn = edge.kind != ManifestEdge.CHANGE || edge.from in changeOnlyNodes || edge.to in changeOnlyNodes
            val from = nodeIds[edge.from]
            val to = nodeIds[edge.to]
            if (!drawn || from == null || to == null) return@forEachIndexed
            val arrow = if (edge.kind == ManifestEdge.OBSERVED) "-->" else "-.->"
            val label = if (edge.kind == ManifestEdge.NOT_WALKED) "${edge.label} (${ManifestEdge.NOT_WALKED})".trim() else edge.label
            val id = if (edgeIds) "e$index@" else ""
            lines += "    $from $id$arrow${labelPart(label)} $to"
        }

        lines += "    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f"
        lines += "    classDef decision fill:#fff7bf,stroke:#b58840"
        lines += "    classDef terminal fill:#cce2d8,stroke:#00703c"
        lines += "    classDef change stroke:#1d70b8,stroke-width:3px"
        if (manifest.entries.isNotEmpty()) {
            lines += "    classDef $START_CLASS fill:#00703c,stroke:#00703c,color:#ffffff"
            lines += "    classDef $ENTRY_CLASS stroke:#00703c,stroke-width:4px"
            lines += "    class $START_ID $START_CLASS"
        }
        val entryNodes = manifest.entries.map { it.node }.toSet()
        manifest.nodes.forEach { node ->
            val id = nodeIds.getValue(node.id)
            val cssClass =
                when {
                    node.kind == ManifestNode.DECISION -> "decision"
                    node.kind == ManifestNode.TERMINAL -> "terminal"
                    node.variants.isEmpty() -> "gap"
                    node.origin == ManifestNode.ORIGIN_CHANGE -> "change"
                    else -> null
                }
            cssClass?.let { lines += "    class $id $it" }
            if (node.id in entryNodes) lines += "    class $id $ENTRY_CLASS"
        }
        return lines.joinToString("\n") + "\n"
    }

    private fun labelPart(label: String) = if (label.isBlank()) "" else "|\"${escape(label)}\"|"

    // A section or task that contains every page of the journey adds a box around the whole map without telling anyone anything
    private fun drawnContainers(manifest: JourneyMapManifest): Set<String> {
        val containedMainNodes = manifest.nodes.filter { it.origin == ManifestNode.ORIGIN_MAIN && it.containers.isNotEmpty() }
        return manifest.containers
            .map { it.id }
            .filterNot { containerId -> containedMainNodes.size > 1 && containedMainNodes.all { containerId in it.containers } }
            .toSet()
    }

    private fun label(
        node: ManifestNode,
        htmlLabels: Boolean,
        thumbnailWidth: Int,
    ): String {
        val variantNote = if (node.variants.size > 1) " (${node.variants.size} variants)" else ""
        val title = escape(node.title) + variantNote
        val thumbnail = node.variants.firstOrNull()?.thumbnail
        return if (htmlLabels && thumbnail != null) {
            val thumbnailHeight = thumbnailWidth * THUMBNAIL_HEIGHT / THUMBNAIL_WIDTH
            "<img src='$thumbnail' width='$thumbnailWidth' height='$thumbnailHeight'/><br/>$title"
        } else {
            title
        }
    }

    private fun shape(
        node: ManifestNode,
        label: String,
    ): String =
        when (node.kind) {
            ManifestNode.DECISION -> "{\"$label\"}"
            ManifestNode.TERMINAL -> "([\"$label\"])"
            else -> "[\"$label\"]"
        }

    private fun escape(text: String): String =
        text
            .replace("\"", "#quot;")
            .replace("<", "#lt;")
            .replace(">", "#gt;")
            .replace("\n", " ")

    companion object {
        const val THUMBNAIL_WIDTH = 384
        const val THUMBNAIL_HEIGHT = 240
        const val START_ID = "journeyStart"
        private const val START_CLASS = "startNode"
        private const val ENTRY_CLASS = "entryPoint"
    }
}
