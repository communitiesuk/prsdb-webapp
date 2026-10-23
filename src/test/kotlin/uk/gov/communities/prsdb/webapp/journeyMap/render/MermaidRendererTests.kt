package uk.gov.communities.prsdb.webapp.journeyMap.render

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.journeyMap.model.JourneyInfo
import uk.gov.communities.prsdb.webapp.journeyMap.model.JourneyMapManifest
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestContainer
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestEdge
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestEntry
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestNode

class MermaidRendererTests {
    private val manifest =
        JourneyMapManifest(
            journey = JourneyInfo("test", "Test journey", "/test", null, "now", emptyMap()),
            containers =
                listOf(
                    ManifestContainer("section:register", "section", "Register", null),
                    ManifestContainer("task:IdentityTask", "task", "Identity", "section:register"),
                ),
            nodes =
                listOf(
                    ManifestNode(
                        "privacy-notice",
                        ManifestNode.PAGE,
                        "privacy-notice",
                        "Is \"this\" ok?",
                        null,
                        listOf("section:register", "task:IdentityTask"),
                        emptyList(),
                    ),
                    ManifestNode("internal:Check", ManifestNode.DECISION, null, "Check user role", null, emptyList(), emptyList()),
                    ManifestNode("/confirmation", ManifestNode.TERMINAL, "/confirmation", "Confirmation", null, emptyList(), emptyList()),
                ),
            edges =
                listOf(
                    ManifestEdge("privacy-notice", "internal:Check", "Agree", null, ManifestEdge.OBSERVED),
                    ManifestEdge("internal:Check", "/confirmation", "", null, ManifestEdge.STRUCTURAL),
                ),
            gaps = emptyList(),
            errors = emptyList(),
        )

    @Test
    fun `renders containers as nested subgraphs and nodes with shapes for their kind`() {
        val mermaid = MermaidRenderer().render(manifest)

        val outer = mermaid.indexOf("subgraph c0[\"Register\"]")
        val inner = mermaid.indexOf("subgraph c1[\"Identity\"]")
        val pageNode = mermaid.indexOf("n0[\"Is #quot;this#quot; ok?\"]")
        assertTrue(mermaid.startsWith("flowchart TD"))
        assertTrue(outer in 0 until inner, mermaid)
        assertTrue(pageNode > inner, mermaid)
        assertTrue(mermaid.contains("n1{\"Check user role\"}"), mermaid)
        assertTrue(mermaid.contains("n2([\"Confirmation\"])"), mermaid)
    }

    @Test
    fun `renders observed edges with labels and structural edges as dotted lines`() {
        val mermaid = MermaidRenderer().render(manifest)

        assertTrue(mermaid.contains("n0 -->|\"Agree\"| n1"), mermaid)
        assertTrue(mermaid.contains("n1 -.-> n2"), mermaid)
    }

    @Test
    fun `renders arrows in the routing code that no walk took as dotted lines marked as not walked`() {
        val notWalked =
            manifest.copy(edges = listOf(ManifestEdge("internal:Check", "/confirmation", "Not a landlord", "NO", ManifestEdge.NOT_WALKED)))

        val mermaid = MermaidRenderer().render(notWalked)

        assertTrue(mermaid.contains("n1 -.->|\"Not a landlord (not walked)\"| n2"), mermaid)
    }

    @Test
    fun `containers that hold every contained main journey page are not drawn`() {
        val wrapped =
            manifest.copy(
                containers = manifest.containers + ManifestContainer("section:only", "section", "Only section", null),
                nodes =
                    listOf(
                        page("a", listOf("section:only", "section:register")),
                        page("b", listOf("section:only")),
                    ),
                edges = emptyList(),
            )

        val mermaid = MermaidRenderer().render(wrapped)

        assertFalse(mermaid.contains("Only section"), mermaid)
        assertTrue(mermaid.contains("subgraph c0[\"Register\"]"), mermaid)
    }

    @Test
    fun `change edges are only drawn when they involve a page seen only while changing answers`() {
        val withChanges =
            manifest.copy(
                containers = emptyList(),
                nodes =
                    listOf(
                        page("check-answers", emptyList()),
                        page("email", emptyList()),
                        page("change:interruption", emptyList(), ManifestNode.ORIGIN_CHANGE),
                    ),
                edges =
                    listOf(
                        ManifestEdge("check-answers", "email", "Change email", null, ManifestEdge.CHANGE),
                        ManifestEdge("check-answers", "change:interruption", "Change type", null, ManifestEdge.CHANGE),
                    ),
            )

        val mermaid = MermaidRenderer().render(withChanges)

        assertFalse(mermaid.contains("Change email"), mermaid)
        assertTrue(mermaid.contains("n0 -.->|\"Change type\"| n2"), mermaid)
    }

    @Test
    fun `draws a start node before everything else, with an arrow to each entry point labelled with how users get there`() {
        val withEntries =
            manifest.copy(entries = listOf(ManifestEntry("privacy-notice", "Change name"), ManifestEntry("internal:Check", "")))

        val mermaid = MermaidRenderer().render(withEntries)

        assertEquals("    journeyStart((\"Start\"))", mermaid.lines()[1])
        assertTrue(mermaid.contains("    journeyStart -->|\"Change name\"| n0"), mermaid)
        assertTrue(mermaid.contains("    journeyStart --> n1"), mermaid)
        assertTrue(mermaid.contains("    class journeyStart startNode"), mermaid)
    }

    @Test
    fun `styles entry points with a green border on top of the style for their kind`() {
        val withEntries = manifest.copy(entries = listOf(ManifestEntry("internal:Check", "")))

        val mermaid = MermaidRenderer().render(withEntries)

        assertTrue(mermaid.contains("    classDef entryPoint stroke:#00703c,stroke-width:4px"), mermaid)
        assertTrue(mermaid.indexOf("    class n1 decision") in 0 until mermaid.indexOf("    class n1 entryPoint"), mermaid)
    }

    @Test
    fun `draws no start node when no entry points were recorded`() {
        val mermaid = MermaidRenderer().render(manifest)

        assertFalse(mermaid.contains("journeyStart"), mermaid)
        assertFalse(mermaid.contains("entryPoint"), mermaid)
    }

    @Test
    fun `gives each arrow an explicit id from its position in the manifest only when asked to`() {
        val withEntries = manifest.copy(entries = listOf(ManifestEntry("privacy-notice", "Change name")))

        val withIds = MermaidRenderer().render(withEntries, edgeIds = true)
        val withoutIds = MermaidRenderer().render(withEntries)

        assertTrue(withIds.contains("    n0 e0@-->|\"Agree\"| n1"), withIds)
        assertTrue(withIds.contains("    n1 e1@-.-> n2"), withIds)
        assertTrue(withIds.contains("    journeyStart s0@-->|\"Change name\"| n0"), withIds)
        assertFalse(withoutIds.contains("@-"), withoutIds)
    }

    @Test
    fun `arrow ids keep their position in the manifest when arrows before them are not drawn`() {
        val withChanges =
            manifest.copy(
                containers = emptyList(),
                nodes =
                    listOf(
                        page("check-answers", emptyList()),
                        page("email", emptyList()),
                        page("change:type", emptyList(), ManifestNode.ORIGIN_CHANGE),
                    ),
                edges =
                    listOf(
                        ManifestEdge("check-answers", "email", "Change email", null, ManifestEdge.CHANGE),
                        ManifestEdge("check-answers", "change:type", "Change type", null, ManifestEdge.CHANGE),
                    ),
            )

        val mermaid = MermaidRenderer().render(withChanges, edgeIds = true)

        assertTrue(mermaid.contains("    n0 e1@-.->|\"Change type\"| n2"), mermaid)
    }

    private fun page(
        id: String,
        containers: List<String>,
        origin: String = ManifestNode.ORIGIN_MAIN,
    ) = ManifestNode(id, ManifestNode.PAGE, id, id, null, containers, emptyList(), origin)
}
