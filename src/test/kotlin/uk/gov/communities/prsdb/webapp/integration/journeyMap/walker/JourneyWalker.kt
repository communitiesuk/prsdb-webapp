package uk.gov.communities.prsdb.webapp.integration.journeyMap.walker

import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.LoadState
import com.microsoft.playwright.options.RequestOptions
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.journeyMap.dependencies.Dependencies
import uk.gov.communities.prsdb.webapp.journeyMap.discovery.PageSnapshot
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectionResponse
import uk.gov.communities.prsdb.webapp.journeyMap.server.JourneyIntrospectionObserver
import uk.gov.communities.prsdb.webapp.journeyMap.server.JourneyMapSessionController
import java.nio.file.Path
import java.security.MessageDigest

data class Navigation(
    val status: Int,
    val method: String,
    val url: String,
    val chain: List<String>,
)

class JourneyWalker(
    private val page: Page,
    port: Int,
) {
    val origin = "http://localhost:$port"
    private val sessionResetsBySnapshot = mutableMapOf<String, Int>()
    private var databaseResets = 0
    private var sessionResets = 0

    // A session that began before the seed data was last restored can refer to records the restore removed
    val sessionPredatesDatabaseReset: Boolean get() = sessionResets < databaseResets

    fun databaseWasReset() {
        databaseResets++
    }

    fun currentUrl(): String = page.url()

    fun clearCookies() {
        page.context().clearCookies()
        sessionResets = databaseResets
    }

    fun navigate(url: String): Navigation = capture { page.navigate(absolute(url)) }

    fun submit(
        action: String,
        params: List<Pair<String, String>>,
    ): Navigation =
        capture {
            page.waitForNavigation(Page.WaitForNavigationOptions().setTimeout(NAVIGATION_TIMEOUT)) {
                page.evaluate(SUBMIT_SCRIPT, listOf(absolute(action), params.map { listOf(it.first, it.second) }))
            }
        }

    fun upload(
        pageUrl: String,
        file: Path,
    ): Navigation {
        navigate(pageUrl)
        page.locator("form[method=post] input[type=file]").first().setInputFiles(file)
        return capture {
            page.waitForNavigation(Page.WaitForNavigationOptions().setTimeout(NAVIGATION_TIMEOUT)) {
                page.locator("form[method=post] button:not([type=button])").first().click()
            }
        }
    }

    fun discover(): PageSnapshot = PageSnapshot.fromJson(page.evaluate(DISCOVER_SCRIPT) as String)

    fun signature(): String {
        val digest = MessageDigest.getInstance("SHA-256").digest((page.evaluate(SIGNATURE_SCRIPT) as String).toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    // Only the heading and form controls, so pages reached while changing answers can be matched with the main journey
    fun structuralSignature(): String {
        val digest = MessageDigest.getInstance("SHA-256").digest((page.evaluate(STRUCTURE_SCRIPT) as String).toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun introspect(url: String): IntrospectionResponse? {
        val response =
            page.request().get(
                absolute(url),
                RequestOptions
                    .create()
                    .setHeader(JourneyIntrospectionObserver.HEADER_NAME, JourneyIntrospectionObserver.HEADER_VALUE)
                    .setMaxRedirects(0),
            )
        return try {
            val contentType = response.headers()["content-type"] ?: ""
            if (response.status() != 200 || !contentType.contains("json")) null else json.decodeFromString(response.text())
        } finally {
            response.dispose()
        }
    }

    fun snapshot(): String = post(JourneyMapSessionController.SNAPSHOT_ROUTE).also { sessionResetsBySnapshot[it] = sessionResets }

    fun dependencies(): Dependencies {
        val response = page.request().get("$origin/${JourneyMapSessionController.DEPENDENCIES_ROUTE}")
        try {
            check(response.ok()) { "GET dependencies failed with status ${response.status()}" }
            return json.decodeFromString(response.text())
        } finally {
            response.dispose()
        }
    }

    // The introspection kept from the last submission in this journey, when that submission left the journey
    fun takeSubmission(journeyId: String): IntrospectionResponse? {
        val response = page.request().post("$origin/${JourneyMapSessionController.SUBMISSION_ROUTE}/$journeyId")
        return try {
            if (response.status() != 200) null else json.decodeFromString(response.text())
        } finally {
            response.dispose()
        }
    }

    fun restore(snapshotId: String) {
        post("${JourneyMapSessionController.RESTORE_ROUTE}/$snapshotId")
        sessionResets = sessionResetsBySnapshot[snapshotId] ?: databaseResets
    }

    fun clearSnapshots() {
        post(JourneyMapSessionController.CLEAR_ROUTE)
    }

    fun absolute(url: String): String = if (url.startsWith("http")) url else origin + (if (url.startsWith("/")) url else "/$url")

    private fun post(route: String): String {
        val response = page.request().post("$origin/$route")
        try {
            check(response.ok()) { "POST /$route failed with status ${response.status()}" }
            return response.text()
        } finally {
            response.dispose()
        }
    }

    private fun capture(navigate: () -> Response?): Navigation {
        val response = navigate()
        page.waitForLoadState(LoadState.LOAD)
        if (response == null) return Navigation(200, "GET", page.url(), emptyList())
        val chain = mutableListOf<String>()
        var request = response.request().redirectedFrom()
        while (request != null) {
            chain.add(0, request.url())
            request = request.redirectedFrom()
        }
        return Navigation(response.status(), response.request().method(), page.url(), chain)
    }

    companion object {
        private const val NAVIGATION_TIMEOUT = 60_000.0

        private val json = Json { ignoreUnknownKeys = true }

        private val DISCOVER_SCRIPT = resource("journey-map/discover-page.js")
        private val SIGNATURE_SCRIPT = resource("journey-map/page-signature.js")
        private val STRUCTURE_SCRIPT =
            """
            () => {
                const main = document.querySelector("main") || document.body;
                const headingElement = main.querySelector("h1") || document.querySelector("h1") || {};
                const heading = (headingElement.textContent || "").replace(/\s+/g, " ").trim();
                const controls = Array.from(main.querySelectorAll("input:not([type=hidden]), select, textarea, button"))
                    .map((element) => {
                        const type = (element.getAttribute("type") || "").toLowerCase();
                        const keepsValue = element.tagName === "BUTTON" || type === "radio" || type === "checkbox";
                        const value = keepsValue ? element.getAttribute("value") || "" : "";
                        return [element.tagName, type, element.getAttribute("name") || "", value].join(":");
                    })
                    .join("|");
                return heading + "#" + controls;
            }
            """.trimIndent()

        // Attributes are set directly because inputs named "action" or "method" shadow the form's properties, and the
        // submission is deferred so that evaluate returns before the navigation begins
        private val SUBMIT_SCRIPT =
            """
            ([action, fields]) => {
                const form = document.createElement("form");
                form.setAttribute("method", "post");
                form.setAttribute("action", action);
                form.style.display = "none";
                for (const [name, value] of fields) {
                    const input = document.createElement("input");
                    input.type = "hidden";
                    input.name = name;
                    input.value = value;
                    form.appendChild(input);
                }
                document.body.appendChild(form);
                setTimeout(() => HTMLFormElement.prototype.submit.call(form), 0);
            }
            """.trimIndent()

        private fun resource(name: String): String =
            JourneyWalker::class.java.classLoader
                .getResource(name)
                ?.readText()
                ?: throw IllegalStateException("$name is missing from the test resources")
    }
}
