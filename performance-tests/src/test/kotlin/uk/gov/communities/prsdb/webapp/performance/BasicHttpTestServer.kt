package uk.gov.communities.prsdb.webapp.performance

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentLinkedQueue

class BasicHttpTestServer(
    private val responseBody: String = "Submission received (no-op)",
    private val responseContentType: String = "text/plain",
    private val postDelayMs: Long = 0,
    private val includeCsrf: Boolean = true,
) : AutoCloseable {
    data class Request(
        val method: String,
        val path: String,
        val cookie: String?,
        val fields: Map<String, String>,
    )

    val requests = ConcurrentLinkedQueue<Request>()
    private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    val baseUrl: String
        get() = "http://127.0.0.1:${server.address.port}"

    init {
        server.createContext("/form") { exchange ->
            val fields =
                exchange.requestBody
                    .readAllBytes()
                    .toString(StandardCharsets.UTF_8)
                    .takeIf { it.isNotEmpty() }
                    ?.split("&")
                    ?.associate {
                        val parts = it.split("=", limit = 2)
                        URLDecoder.decode(parts[0], StandardCharsets.UTF_8) to
                            URLDecoder.decode(parts.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                    }.orEmpty()
            val cookie = exchange.requestHeaders.getFirst("Cookie")
            requests.add(Request(exchange.requestMethod, exchange.requestURI.toString(), cookie, fields))

            val (status, body, contentType) =
                when (exchange.requestMethod) {
                    "GET" -> {
                        exchange.responseHeaders.add("Set-Cookie", "SESSION=synthetic-session; Path=/; HttpOnly")
                        val csrf = if (includeCsrf) """<input type="hidden" name="_csrf" value="synthetic-csrf"/>""" else ""
                        Triple(200, """<form method="post" action="/form?journeyId=synthetic">$csrf</form>""", "text/html")
                    }
                    "POST" -> {
                        if (postDelayMs > 0) Thread.sleep(postDelayMs)
                        if (
                            cookie?.contains("SESSION=synthetic-session") == true &&
                            fields["_csrf"] == "synthetic-csrf" &&
                            exchange.requestURI.rawQuery == "journeyId=synthetic"
                        ) {
                            Triple(200, responseBody, responseContentType)
                        } else {
                            Triple(403, "Invalid session, CSRF or journey", "text/plain")
                        }
                    }
                    else -> Triple(405, "Method not allowed", "text/plain")
                }
            val bytes = body.toByteArray(StandardCharsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", contentType)
            exchange.sendResponseHeaders(status, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
    }

    override fun close() {
        server.stop(0)
    }
}
