package uk.gov.communities.prsdb.webapp.integration.journeyMap.output

import com.microsoft.playwright.Page
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.integration.journeyMap.JourneyMapHarness
import uk.gov.communities.prsdb.webapp.integration.journeyMap.JourneyMapSettings
import uk.gov.communities.prsdb.webapp.integration.journeyMap.descriptors.JourneyDescriptor
import uk.gov.communities.prsdb.webapp.integration.journeyMap.explorer.JourneyExplorer
import uk.gov.communities.prsdb.webapp.integration.journeyMap.registry.JourneyMapRegistry
import uk.gov.communities.prsdb.webapp.integration.journeyMap.walker.JourneyWalker
import uk.gov.communities.prsdb.webapp.integration.journeyMap.walker.ScreenshotRenderer
import uk.gov.communities.prsdb.webapp.integration.journeyMap.walker.VariantRenderer
import uk.gov.communities.prsdb.webapp.journeyMap.discovery.ValueGenerator
import uk.gov.communities.prsdb.webapp.journeyMap.graph.ManifestAssembler
import uk.gov.communities.prsdb.webapp.journeyMap.model.JourneyInfo
import uk.gov.communities.prsdb.webapp.journeyMap.model.JourneyMapManifest
import uk.gov.communities.prsdb.webapp.journeyMap.render.HtmlViewerWriter
import uk.gov.communities.prsdb.webapp.journeyMap.render.MermaidRenderer
import uk.gov.communities.prsdb.webapp.journeyMap.render.ReadmeRenderer
import uk.gov.communities.prsdb.webapp.journeyMap.render.ReportRenderer
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

data class GenerationResult(
    val manifest: JourneyMapManifest,
    val usedScenarioOptions: Set<String>,
    val duration: Duration,
) {
    val problems: List<String>
        get() =
            manifest.gaps.map { "${manifest.journey.name}: gap at ${it.node}${it.mode?.let { mode -> " ($mode)" } ?: ""} - ${it.reason}" } +
                manifest.errors.map { "${manifest.journey.name}: ${it.kind} error at ${it.node} - ${it.detail}" }

    val summary: String
        get() =
            "${manifest.journey.name}: ${manifest.nodes.size} nodes, ${manifest.nodes.sumOf { it.variants.size }} screenshots, " +
                "${manifest.edges.size} edges, ${manifest.gaps.size} gaps, ${manifest.errors.size} errors in ${duration.toSeconds()}s"
}

class JourneyMapGenerator(
    private val harness: JourneyMapHarness,
    private val page: Page,
    private val settings: JourneyMapSettings,
) {
    fun generate(
        descriptor: JourneyDescriptor,
        writeOutput: Boolean = true,
    ): GenerationResult {
        val started = System.nanoTime()
        val outputDir = settings.outputDir.resolve(descriptor.name)
        if (writeOutput) {
            outputDir.toFile().deleteRecursively()
            Files.createDirectories(outputDir)
        }

        harness.resetDatabase(descriptor.seedScripts)
        harness.applyBaseFeatureFlags()
        JourneyMapRegistry.applyDefaults(harness)
        descriptor.arrange(harness)
        val featureFlags = harness.featureFlagStates()
        page.context().clearCookies()
        page.setViewportSize(DESKTOP_WIDTH, DESKTOP_HEIGHT)

        val walker = JourneyWalker(page, harness.port)
        val renderer =
            if (writeOutput && settings.screenshots && !settings.structureOnly) {
                ScreenshotRenderer(page, outputDir, settings.mobile)
            } else {
                VariantRenderer.NONE
            }
        val values = ValueGenerator(LocalDate.now(), descriptor.address ?: harness.firstSeededAddress())
        val explorer = JourneyExplorer(walker, descriptor, harness, values, renderer, settings)
        val exploration =
            try {
                explorer.explore()
            } finally {
                runCatching { walker.clearSnapshots() }
            }

        val journey =
            JourneyInfo(descriptor.name, descriptor.title, descriptor.basePath, gitCommit(), Instant.now().toString(), featureFlags)
        val manifest = ManifestAssembler().assemble(journey, exploration)
        if (writeOutput) writeOutputs(manifest, outputDir, walker.origin)
        return GenerationResult(manifest, explorer.usedScenarioOptions, Duration.ofNanos(System.nanoTime() - started))
    }

    private fun writeOutputs(
        manifest: JourneyMapManifest,
        outputDir: Path,
        origin: String,
    ) {
        Files.writeString(outputDir.resolve("manifest.json"), prettyJson.encodeToString(manifest))
        Files.writeString(outputDir.resolve("README.md"), ReadmeRenderer().render(manifest))
        Files.writeString(outputDir.resolve("report.md"), ReportRenderer().render(manifest))
        val svg =
            try {
                MermaidSvgRenderer(page, origin, settings.nodeModules)
                    .render(MermaidRenderer().render(manifest, htmlLabels = true, edgeIds = true), outputDir)
            } catch (exception: Exception) {
                println("[journey-map] Drawing the map failed: ${exception.message}")
                null
            }
        Files.writeString(outputDir.resolve("index.html"), HtmlViewerWriter().render(manifest, svg))
    }

    private fun gitCommit(): String? =
        runCatching {
            val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD").redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().readText().trim()
            if (process.waitFor() == 0) output else null
        }.getOrNull()

    companion object {
        private const val DESKTOP_WIDTH = 1280
        private const val DESKTOP_HEIGHT = 800

        private val prettyJson =
            Json {
                prettyPrint = true
                encodeDefaults = true
            }
    }
}
