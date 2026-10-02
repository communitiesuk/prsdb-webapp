package uk.gov.communities.prsdb.webapp.performance

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BasicHttpContractTests {
    @TempDir
    lateinit var outputDirectory: Path

    @Test
    fun `Gatling preserves cookies CSRF journey parameters and form encoding`() {
        BasicHttpTestServer().use { server ->
            val result = runSimulation(server)

            assertEquals(0, result.exitCode, result.output)
            assertEquals(listOf("GET", "POST"), server.requests.map { it.method })
            val submittedRequest = server.requests.last()
            assertEquals("synthetic-csrf", submittedRequest.fields["_csrf"])
            assertEquals("value + & spaces", submittedRequest.fields["value"])
            assertEquals("/form?journeyId=synthetic", submittedRequest.path)
            assertTrue(submittedRequest.cookie.orEmpty().contains("SESSION=synthetic-session"))
        }
    }

    @Test
    fun `skipped expected request fails even when all executed requests pass`() {
        BasicHttpTestServer().use { server ->
            val result = runSimulation(server, mapOf("gatling.contract.skipSubmit" to "true"))

            assertEquals(listOf("GET"), server.requests.map { it.method })
            assertEquals(2, result.exitCode, result.output)
        }
    }

    @Test
    fun `wrong response body fails the runner even with status 200`() {
        BasicHttpTestServer(responseBody = "Unexpected response").use { server ->
            val result = runSimulation(server)

            assertEquals(listOf("GET", "POST"), server.requests.map { it.method })
            assertEquals(2, result.exitCode, result.output)
        }
    }

    @Test
    fun `wrong content type fails the runner even with expected body`() {
        BasicHttpTestServer(responseContentType = "text/html").use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
        }
    }

    @Test
    fun `missing CSRF fails the runner and prevents mutation`() {
        BasicHttpTestServer(includeCsrf = false).use { server ->
            val result = runSimulation(server)

            assertEquals(listOf("GET"), server.requests.map { it.method })
            assertEquals(2, result.exitCode, result.output)
        }
    }

    @Test
    fun `baseline records slow correct responses without failing a timing gate`() {
        BasicHttpTestServer(postDelayMs = 250).use { server ->
            val result = runSimulation(server)

            assertEquals(0, result.exitCode, result.output)
            assertEquals(2, server.requests.size)
        }
    }

    @Test
    fun `maximum response time gate fails slow requests`() {
        BasicHttpTestServer(postDelayMs = 250).use { server ->
            val result =
                runSimulation(
                    server,
                    mapOf(
                        "gatling.basic.mode" to "gated",
                        "gatling.local.basic.maxResponseTimeMs" to "100",
                        "gatling.local.basic.meanResponseTimeMs" to "100",
                    ),
                )

            assertEquals(2, server.requests.size)
            assertEquals(2, result.exitCode, result.output)
        }
    }

    @Test
    fun `mean response time gate fails independently of the maximum gate`() {
        BasicHttpTestServer(postDelayMs = 250).use { server ->
            val result =
                runSimulation(
                    server,
                    mapOf(
                        "gatling.basic.mode" to "gated",
                        "gatling.local.basic.maxResponseTimeMs" to "10000",
                        "gatling.local.basic.meanResponseTimeMs" to "100",
                    ),
                )

            assertEquals(2, result.exitCode, result.output)
        }
    }

    @Test
    fun `gated mode passes correct responses within explicit limits`() {
        BasicHttpTestServer().use { server ->
            val result =
                runSimulation(
                    server,
                    mapOf(
                        "gatling.basic.mode" to "gated",
                        "gatling.local.basic.maxResponseTimeMs" to "10000",
                        "gatling.local.basic.meanResponseTimeMs" to "10000",
                    ),
                )

            assertEquals(0, result.exitCode, result.output)
        }
    }

    private fun runSimulation(
        server: BasicHttpTestServer,
        extraProperties: Map<String, String> = emptyMap(),
    ): GatlingTestRunner.Result {
        val properties =
            mapOf(
                "gatling.target" to "local",
                "gatling.baseUrl" to server.baseUrl,
                "gatling.simulatorUrl" to server.baseUrl,
                "gatling.basic.landlordSubject" to "synthetic-landlord",
                "gatling.basic.mode" to "baseline",
            ) + extraProperties
        return GatlingTestRunner(outputDirectory).run(BasicContractSimulation::class.java, properties)
    }
}
