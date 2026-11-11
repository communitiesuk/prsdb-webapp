package uk.gov.communities.prsdb.webapp.performance

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CouncilHttpContractTests {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `invitation missing on first management page is found on second page and cancelled`() {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        var email = ""
        var cancelled = false
        val pages = ConcurrentLinkedQueue<String>()
        val invite = "/local-council/2/invite-new-user"
        val cancel = "/local-council/2/cancel-invitation/73"
        server.createContext("/") { exchange ->
            val path = exchange.requestURI.path
            var code = 200
            var body = ""
            val post = exchange.requestMethod == "POST"
            val fields =
                if (post) {
                    exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8)
                        .split("&").associate {
                            val pair = it.split("=", limit = 2)
                            URLDecoder.decode(pair[0], StandardCharsets.UTF_8) to
                                URLDecoder.decode(pair.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                        }
                } else {
                    emptyMap()
                }
            when {
                path == invite && post -> {
                    email = fields.getValue("email")
                    code = 302
                    exchange.responseHeaders.add("Location", "$invite/confirmation")
                }
                path == cancel && post -> {
                    cancelled = fields["_csrf"] == "csrf"
                    code = 302
                    exchange.responseHeaders.add("Location", "$cancel/confirmation")
                }
                path == cancel && cancelled -> code = 404
                path.endsWith("/confirmation") -> body = "<main>$email</main>"
                path.endsWith("/manage-users") -> {
                    val page = exchange.requestURI.rawQuery.split('&').single { it.startsWith("page=") }
                    pages.add(page)
                    body =
                        if (page == "page=2") {
                            """<main><a href="$cancel">$email</a></main>"""
                        } else {
                            "<main>No matching invitation on page one</main>"
                        }
                }
                else ->
                    body = """<main>$email<form action="$path" method="post">""" +
                        """<input type="hidden" name="_csrf" value="csrf"></form></main>"""
            }
            val bytes = body.toByteArray(StandardCharsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "text/html")
            exchange.sendResponseHeaders(code, if (bytes.isEmpty()) -1 else bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        try {
            ManagementServer().use { placeholder ->
                val result = run(placeholder, "invitation", "http://127.0.0.1:${server.address.port}")
                assertEquals(0, result.exitCode, result.output)
            }
            assertEquals(listOf("page=1", "page=2"), pages.toList())
            assertTrue(cancelled)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `access changes preserve hidden fields cookie and target and verify persisted role`() {
        ManagementServer().use { server ->
            val result = run(server)
            assertEquals(0, result.exitCode, result.output)
            assertEquals(listOf("true", "false"), server.posts.map { it["isManager"] })
        }
    }

    @Test
    fun `access changes verify fresh roles even when GET responses are cached`() {
        ManagementServer(cacheReads = true).use { server ->
            val result = run(server)
            assertEquals(0, result.exitCode, result.output)
            assertEquals(listOf("true", "false"), server.posts.map { it["isManager"] })
        }
    }

    @Test
    fun `missing CSRF or a foreign form action stops before mutation`() {
        for (foreignAction in listOf(false, true)) {
            ManagementServer(includeCsrf = foreignAction, foreignAction = foreignAction).use { server ->
                val result = run(server)
                assertEquals(2, result.exitCode, result.output)
                assertTrue(server.posts.isEmpty())
            }
        }
    }

    @Test
    fun `success status without a durable access change fails`() {
        ManagementServer(persist = false).use { server ->
            val result = run(server)
            assertEquals(2, result.exitCode, result.output)
            assertEquals(1, server.posts.size)
        }
    }

    private fun run(
        server: ManagementServer,
        mode: String = "access",
        baseUrl: String = server.baseUrl,
    ): GatlingTestRunner.Result {
        return GatlingTestRunner(directory).run(
            Class.forName("uk.gov.communities.prsdb.webapp.performance.CouncilContractSimulation"),
            mapOf(
                "gatling.target" to "local",
                "gatling.baseUrl" to baseUrl,
                "gatling.simulatorUrl" to baseUrl,
                "gatling.contract.council" to mode,
                "gatling.basic.mode" to "baseline",
                "gatling.basic.iterations" to "1",
                "gatling.basic.landlordSubject" to "contract-only",
            ),
        )
    }

    private class ManagementServer(
        private val includeCsrf: Boolean = true,
        private val foreignAction: Boolean = false,
        private val persist: Boolean = true,
        private val cacheReads: Boolean = false,
    ) : AutoCloseable {
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl = "http://127.0.0.1:${server.address.port}"
        val posts = ConcurrentLinkedQueue<Map<String, String>>()
        private var manager = false
        private val edit = "/local-council/2/edit-user/42"
        private val cachedPages = mutableMapOf<String, String>()

        init {
            server.createContext("/") { exchange ->
                var code = 200
                var body = ""
                when {
                    exchange.requestURI.path != edit -> code = 404
                    exchange.requestMethod == "POST" -> {
                        val fields =
                            exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8)
                                .split("&").associate {
                                    val pair = it.split("=", limit = 2)
                                    URLDecoder.decode(pair[0], StandardCharsets.UTF_8) to
                                        URLDecoder.decode(pair.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                                }
                        posts.add(fields)
                        if (fields["_csrf"] != "access-csrf" || fields["version"] != "current" ||
                            exchange.requestHeaders.getFirst("Cookie") != "SESSION=council-contract" ||
                            exchange.requestURI.rawQuery != "correlation=target-42"
                        ) {
                            code = 403
                        } else {
                            if (persist) manager = fields["isManager"] == "true"
                            code = 302
                            exchange.responseHeaders.add("Location", "/local-council/2/manage-users")
                        }
                    }
                    else -> {
                        exchange.responseHeaders.add("Set-Cookie", "SESSION=council-contract; Path=/")
                        val action = if (foreignAction) "https://example.invalid/" else "$edit?correlation=target-42"
                        body = """<main><h1>Fixture Council User</h1><p>fixture@example.com</p>
                            <form method="post" action="$action">
                            ${if (includeCsrf) """<input type="hidden" name="_csrf" value="access-csrf">""" else ""}
                            <input type="hidden" name="version" value="current">
                            <input type="radio" name="isManager" value="true" ${if (manager) "checked" else ""}>
                            <input type="radio" name="isManager" value="false" ${if (!manager) "checked" else ""}>
                            </form></main>"""
                        if (cacheReads) {
                            body = cachedPages.getOrPut(exchange.requestURI.toString()) { body }
                        }
                    }
                }
                exchange.responseHeaders.add("Content-Type", "text/html")
                val bytes = body.toByteArray(StandardCharsets.UTF_8)
                exchange.sendResponseHeaders(code, if (bytes.isEmpty()) -1 else bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            server.start()
        }

        override fun close() = server.stop(0)
    }
}
