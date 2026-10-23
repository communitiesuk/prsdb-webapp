package uk.gov.communities.prsdb.webapp.integration.journeyMap.output

import com.microsoft.playwright.Page
import com.microsoft.playwright.Route
import java.nio.file.Files
import java.nio.file.Path

// Draws the map with Mermaid and the ELK layout in the test browser, serving the npm packages through a route
class MermaidSvgRenderer(
    private val page: Page,
    private val origin: String,
    private val nodeModules: Path,
) {
    fun render(
        definition: String,
        assetsDir: Path,
    ): String? {
        if (!Files.isDirectory(nodeModules.resolve("mermaid"))) {
            println("[journey-map] Mermaid is not installed in $nodeModules, so index.html will only show the pages view")
            return null
        }
        val prefix = "$origin/__journey-map__/"
        val pattern = "$prefix**"
        page.route(pattern) { route ->
            serve(route, route.request().url().removePrefix(prefix).substringBefore('?'), definition, assetsDir)
        }
        try {
            page.navigate("${prefix}render.html")
            page.waitForFunction("() => window.__journeyMapDone === true", null, Page.WaitForFunctionOptions().setTimeout(RENDER_TIMEOUT))
            val error = page.evaluate("() => window.__journeyMapError || null") as String?
            if (error != null) {
                println("[journey-map] Mermaid could not draw the map: $error")
                return null
            }
            return page.evaluate("() => window.__journeyMapSvg") as String
        } finally {
            page.unroute(pattern)
        }
    }

    private fun serve(
        route: Route,
        relativePath: String,
        definition: String,
        assetsDir: Path,
    ) {
        val body =
            when {
                relativePath == "render.html" -> RENDER_PAGE.toByteArray()
                relativePath == "definition.mmd" -> definition.toByteArray()
                relativePath.startsWith("node_modules/") -> read(nodeModules, relativePath.removePrefix("node_modules/"))
                else -> read(assetsDir, relativePath)
            }
        if (body == null) {
            route.fulfill(Route.FulfillOptions().setStatus(404))
        } else {
            route.fulfill(Route.FulfillOptions().setStatus(200).setContentType(contentType(relativePath)).setBodyBytes(body))
        }
    }

    private fun read(
        root: Path,
        relativePath: String,
    ): ByteArray? {
        val normalisedRoot = root.toAbsolutePath().normalize()
        val file = normalisedRoot.resolve(relativePath).normalize()
        return if (file.startsWith(normalisedRoot) && Files.isRegularFile(file)) Files.readAllBytes(file) else null
    }

    private fun contentType(path: String) =
        when (path.substringAfterLast('.').lowercase()) {
            "html" -> "text/html"
            "js", "mjs" -> "text/javascript"
            "css" -> "text/css"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "svg" -> "image/svg+xml"
            "json" -> "application/json"
            else -> "text/plain"
        }

    companion object {
        private const val RENDER_TIMEOUT = 180_000.0

        private val RENDER_PAGE =
            """
            <!doctype html>
            <html lang="en">
            <head><meta charset="utf-8"><style>body { margin: 0; font-family: Arial, Helvetica, sans-serif; }</style></head>
            <body>
            <script type="module">
                import mermaid from "./node_modules/mermaid/dist/mermaid.esm.min.mjs";
                import elkLayouts from "./node_modules/@mermaid-js/layout-elk/dist/mermaid-layout-elk.esm.min.mjs";
                try {
                    mermaid.registerLayoutLoaders(elkLayouts);
                    mermaid.initialize({
                        startOnLoad: false,
                        securityLevel: "loose",
                        maxTextSize: 50000000,
                        maxEdges: 100000,
                        layout: "elk",
                        fontFamily: "Arial, Helvetica, sans-serif",
                        flowchart: { htmlLabels: true, nodeSpacing: 40, rankSpacing: 70 },
                    });
                    const definition = await (await fetch("./definition.mmd")).text();
                    const { svg } = await mermaid.render("journey-map-svg", definition);
                    window.__journeyMapSvg = svg;
                } catch (error) {
                    window.__journeyMapError = String((error && error.stack) || error);
                }
                window.__journeyMapDone = true;
            </script>
            </body>
            </html>
            """.trimIndent()
    }
}
