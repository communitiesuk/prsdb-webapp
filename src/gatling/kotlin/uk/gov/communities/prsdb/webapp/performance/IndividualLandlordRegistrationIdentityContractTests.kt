package uk.gov.communities.prsdb.webapp.performance

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IndividualLandlordRegistrationIdentityContractTests {
    @TempDir
    lateinit var outputDirectory: Path

    @Test
    fun `unverified identity submits a valid simulator credential without requesting the core identity claim`() {
        IdentityVerificationServer().use { server ->
            val result = runSimulation(server)

            assertEquals(0, result.exitCode, result.output)
            assertEquals(1, server.submissions.size)
            val mapper = ObjectMapper()
            val submittedCredential = mapper.readTree(server.submissions.single().getValue("coreIdentity"))
            assertTrue(submittedCredential.has("credentialSubject"))
            val claims = mapper.readTree(server.authorizationClaims.single()).get("userinfo")
            assertFalse(claims.has("https://vocab.account.gov.uk/v1/coreIdentityJWT"))
            assertTrue(claims.has("https://vocab.account.gov.uk/v1/address"))
            assertTrue(claims.has("https://vocab.account.gov.uk/v1/returnCode"))
        }
    }

    private fun runSimulation(server: IdentityVerificationServer): GatlingTestRunner.Result =
        GatlingTestRunner(outputDirectory).run(
            IdentityVerificationContractSimulation::class.java,
            mapOf(
                "gatling.target" to "local",
                "gatling.baseUrl" to server.baseUrl,
                "gatling.simulatorUrl" to server.baseUrl,
                "gatling.basic.landlordSubject" to "synthetic-unverified-landlord",
                "gatling.basic.mode" to "baseline",
                "gatling.basic.iterations" to "1",
            ),
        )

    private class IdentityVerificationServer : AutoCloseable {
        val submissions = ConcurrentLinkedQueue<Map<String, String>>()
        val authorizationClaims = ConcurrentLinkedQueue<String>()
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl = "http://127.0.0.1:${server.address.port}"
        private val claims =
            """{"userinfo":{
                "https://vocab.account.gov.uk/v1/coreIdentityJWT":{"essential":true},
                "https://vocab.account.gov.uk/v1/address":{"essential":true},
                "https://vocab.account.gov.uk/v1/returnCode":{"essential":true}
            }}"""

        init {
            server.createContext("/") { exchange ->
                val path = exchange.requestURI.path
                val method = exchange.requestMethod
                val fields =
                    exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8)
                        .split("&")
                        .filter { it.isNotBlank() }
                        .associate {
                            val parts = it.split("=", limit = 2)
                            URLDecoder.decode(parts[0], StandardCharsets.UTF_8) to
                                URLDecoder.decode(parts.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                        }
                var status = 200
                var body = ""

                fun redirect(location: String) {
                    status = 302
                    exchange.responseHeaders.add("Location", location)
                }

                when {
                    method == "GET" && path == "/id-verification/oauth2/authorize/one-login" ->
                        redirect("/oauth2/authorization/one-login")

                    method == "GET" && path == "/oauth2/authorization/one-login" ->
                        redirect(
                            "$baseUrl/authorize?claims=${encode(claims)}&vtr=${encode("""["Cl.Cm.P2"]""")}",
                        )

                    method == "GET" && path == "/authorize" -> {
                        val query =
                            exchange.requestURI.rawQuery.orEmpty().split("&").associate {
                                val parts = it.split("=", limit = 2)
                                URLDecoder.decode(parts[0], StandardCharsets.UTF_8) to
                                    URLDecoder.decode(parts.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                            }
                        authorizationClaims.add(requireNotNull(query["claims"]))
                        body =
                            """<form method="post" action="$baseUrl/simulator/continue">
                                <input type="hidden" name="state" value="contract-state">
                                <input data-testid="sub" name="sub">
                                <textarea data-testid="core-identity-vc" name="coreIdentity"></textarea>
                                <textarea data-testid="postal-address-details" name="address">[]</textarea>
                                <textarea data-testid="return-codes" name="returnCodes">[{"code":"00"}]</textarea>
                            </form>"""
                    }

                    method == "POST" && path == "/simulator/continue" -> {
                        submissions.add(fields)
                        try {
                            require(ObjectMapper().readTree(fields.getValue("coreIdentity")).isObject)
                            redirect("/login/oauth2/code/one-login?code=contract-code")
                        } catch (_: Exception) {
                            status = 400
                            body = """{"error":"invalid_request","invalid_fields":[{"field":"coreIdentity"}]}"""
                        }
                    }

                    method == "GET" && path == "/login/oauth2/code/one-login" ->
                        redirect("/register/resume?journeyId=identity-contract")

                    method == "GET" && path == "/register/resume" ->
                        redirect("/register/identity-not-verified?journeyId=identity-contract")

                    else -> status = 404
                }
                val bytes = body.toByteArray(StandardCharsets.UTF_8)
                exchange.responseHeaders.add("Content-Type", "text/html")
                exchange.sendResponseHeaders(status, if (bytes.isEmpty()) -1 else bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            server.start()
        }

        private fun encode(value: String) = java.net.URLEncoder.encode(value, StandardCharsets.UTF_8)

        override fun close() = server.stop(0)
    }
}
