package uk.gov.communities.prsdb.webapp.performance

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals

class OperatorHttpContractTests {
    @Test
    fun `passcodes correlate CSRF and cookie and persist in the session`() {
        PasscodeServer().use { server ->
            val result = run(server)
            assertEquals(0, result.exitCode, result.output)
            assertEquals(1, server.posts.get())
        }
    }

    @Test
    fun `unchanged generated passcode or missing CSRF fails`() {
        for (csrf in listOf(false, true)) {
            PasscodeServer(includeCsrf = csrf, changeCode = !csrf).use { server ->
                val result = run(server)
                assertEquals(2, result.exitCode, result.output)
                assertEquals(if (csrf) 1 else 0, server.posts.get())
            }
        }
    }

    @Test
    fun `a passcode displayed from session but absent from the database fails validation`() {
        PasscodeServer(storeCode = false).use { server ->
            val result = run(server)
            assertEquals(2, result.exitCode, result.output)
            assertEquals(1, server.posts.get())
        }
    }

    @Test
    fun `invalid metrics reject metric rows but allow unrelated layout summary lists`() {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            val metrics = exchange.requestURI.path == "/system-operator/metrics"
            val isPost = exchange.requestMethod == "POST"
            val body =
                """<html><body><dl class="govuk-summary-list"><div class="govuk-summary-list__row">Cookie preferences</div></dl>
                <main>
                <a href="/system-operator/invite-lc-admin">Invite</a><a href="/system-operator/metrics">Metrics</a>
                ${if (metrics && isPost) """<div class="govuk-error-summary">Enter a real date</div>""" else ""}
                <form method="post" action="${exchange.requestURI.path}">
                <input type="hidden" name="_csrf" value="csrf"><input type="hidden" name="version" value="current">
                </form></main></body></html>""".toByteArray(StandardCharsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "text/html")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
        try {
            val result = run("http://127.0.0.1:${server.address.port}", "dashboard")
            assertEquals(0, result.exitCode, result.output)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `metrics require a correlated reporting period and actual metric rows not just success status`() {
        for (includeRows in listOf(true, false)) {
            val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
            val posts = AtomicInteger()
            val to = LocalDate.now().minusDays(1)
            val from = to.minusMonths(1)
            server.createContext("/system-operator/metrics") { exchange ->
                var code = 200
                val isPost = exchange.requestMethod == "POST"
                if (isPost) {
                    posts.incrementAndGet()
                    val fields =
                        exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8).split("&").associate {
                            val pair = it.split("=", limit = 2)
                            URLDecoder.decode(pair[0], StandardCharsets.UTF_8) to
                                URLDecoder.decode(pair.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                        }
                    if (fields["_csrf"] != "metrics-csrf" || fields["version"] != "current" ||
                        fields["fromDay"] != from.dayOfMonth.toString() || fields["toDay"] != to.dayOfMonth.toString() ||
                        exchange.requestHeaders.getFirst("Cookie") != "SESSION=metrics-contract"
                    ) {
                        code = 403
                    }
                }
                exchange.responseHeaders.add("Content-Type", "text/html")
                exchange.responseHeaders.add("Set-Cookie", "SESSION=metrics-contract; Path=/")
                val rows =
                    if (isPost && includeRows) {
                        (1..19).joinToString("") { """<div class="govuk-summary-list__row"><dt>Metric $it</dt><dd>$it</dd></div>""" }
                    } else {
                        ""
                    }
                val body =
                    """<form method="post" action="/system-operator/metrics">
                    <input type="hidden" name="_csrf" value="metrics-csrf"><input type="hidden" name="version" value="current">
                    <input name="fromYear" value="${from.year}"><input name="toYear" value="${to.year}"></form>$rows"""
                        .toByteArray(StandardCharsets.UTF_8)
                exchange.sendResponseHeaders(code, body.size.toLong())
                exchange.responseBody.use { it.write(body) }
            }
            server.start()
            try {
                val result = run("http://127.0.0.1:${server.address.port}", "metrics")
                assertEquals(if (includeRows) 0 else 2, result.exitCode, result.output)
                assertEquals(1, posts.get())
            } finally {
                server.stop(0)
            }
        }
    }

    private fun run(server: PasscodeServer): GatlingTestRunner.Result = run(server.baseUrl, "passcode")

    private fun run(
        baseUrl: String,
        journey: String,
    ): GatlingTestRunner.Result {
        val directory =
            Files.createDirectories(
                Path.of(
                    System.getProperty("user.home"),
                    ".copilot",
                    "session-state",
                    "0adcbe66-1747-4ffa-ba2b-ff54b2db6dfe",
                    "files",
                    "operator-contract-${UUID.randomUUID()}",
                ),
            )
        return GatlingTestRunner(directory).run(
            Class.forName("uk.gov.communities.prsdb.webapp.performance.OperatorContractSimulation"),
            mapOf(
                "gatling.target" to "local",
                "gatling.baseUrl" to baseUrl,
                "gatling.simulatorUrl" to baseUrl,
                "gatling.contract.operator" to journey,
                "gatling.basic.mode" to "baseline",
                "gatling.basic.iterations" to "1",
                "gatling.basic.landlordSubject" to "contract-only",
            ),
        )
    }

    private class PasscodeServer(
        private val includeCsrf: Boolean = true,
        private val changeCode: Boolean = true,
        private val storeCode: Boolean = true,
    ) : AutoCloseable {
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl = "http://127.0.0.1:${server.address.port}"
        val posts = AtomicInteger()
        private var passcode = "CDF234"

        init {
            server.createContext("/system-operator/generate-passcode") { exchange ->
                var status = 200
                if (exchange.requestMethod == "POST") {
                    posts.incrementAndGet()
                    val fields =
                        exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8).split("&").associate {
                            val pair = it.split("=", limit = 2)
                            URLDecoder.decode(pair[0], StandardCharsets.UTF_8) to
                                URLDecoder.decode(pair.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                        }
                    if (fields != mapOf("_csrf" to "operator-csrf", "version" to "current") ||
                        exchange.requestHeaders.getFirst("Cookie") != "SESSION=operator-contract"
                    ) {
                        status = 403
                    } else if (changeCode) {
                        passcode = "GHJ567"
                    }
                }
                exchange.responseHeaders.add("Set-Cookie", "SESSION=operator-contract; Path=/")
                exchange.responseHeaders.add("Content-Type", "text/html")
                val body =
                    """<h1>Passcode generated</h1>
                    <div class="govuk-panel__body"><strong>$passcode</strong></div>
                    <form method="post" action="/system-operator/generate-passcode">
                    ${if (includeCsrf) """<input type="hidden" name="_csrf" value="operator-csrf">""" else ""}
                    <input type="hidden" name="version" value="current"></form>""".toByteArray(StandardCharsets.UTF_8)
                exchange.sendResponseHeaders(status, body.size.toLong())
                exchange.responseBody.use { it.write(body) }
            }
            server.createContext("/landlord/passcode-entry") { exchange ->
                var code = 200
                if (exchange.requestMethod == "POST") {
                    val fields =
                        exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8).split("&").associate {
                            val pair = it.split("=", limit = 2)
                            URLDecoder.decode(pair[0], StandardCharsets.UTF_8) to
                                URLDecoder.decode(pair.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                        }
                    if (storeCode && fields["passcode"] == passcode && fields["_csrf"] == "operator-csrf" &&
                        fields["version"] == "current" && exchange.requestHeaders.getFirst("Cookie") == "SESSION=operator-contract"
                    ) {
                        code = 302
                        exchange.responseHeaders.add("Location", "/landlord/register-as-a-landlord")
                    }
                }
                exchange.responseHeaders.add("Content-Type", "text/html")
                val body =
                    """<form method="post" action="/landlord/passcode-entry">
                    <input type="hidden" name="_csrf" value="operator-csrf">
                    <input type="hidden" name="version" value="current"><input name="passcode"></form>"""
                        .toByteArray(StandardCharsets.UTF_8)
                exchange.sendResponseHeaders(code, body.size.toLong())
                exchange.responseBody.use { it.write(body) }
            }
            server.start()
        }

        override fun close() = server.stop(0)
    }
}
