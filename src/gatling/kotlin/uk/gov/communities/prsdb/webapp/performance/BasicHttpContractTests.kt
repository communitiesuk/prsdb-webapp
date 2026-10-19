package uk.gov.communities.prsdb.webapp.performance

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals

class BasicHttpContractTests {
    @TempDir
    lateinit var outputDirectory: Path

    @Test
    fun `skipped expected request fails even when all executed requests pass`() {
        BasicHttpTestServer().use { server ->
            val result = runSimulation(server, mapOf("gatling.contract.skipSubmit" to "true"))

            assertEquals(listOf("GET", "GET", "GET"), server.requests.map { it.method })
            assertEquals(2, result.exitCode, result.output)
        }
    }

    @Test
    fun `wrong response body fails the runner even with status 200`() {
        BasicHttpTestServer(responseBody = "Unexpected response").use { server ->
            val result = runSimulation(server)

            assertEquals(2, server.requests.count { it.method == "GET" })
            assertEquals(1, server.requests.count { it.method == "POST" })
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
            assertEquals(6, server.requests.size)
        }
    }

    @Test
    fun `mean response time gate fails slow requests when maximum budget is generous`() {
        BasicHttpTestServer(postDelaysMs = listOf(0, 0, 250)).use { server ->
            val result =
                runSimulation(
                    server,
                    mapOf(
                        "gatling.basic.mode" to "gated",
                        "gatling.local.basic.meanResponseTimeMs" to "100",
                        "gatling.local.basic.maxResponseTimeMs" to "1000",
                    ),
                )

            assertEquals(6, server.requests.size)
            assertEquals(2, result.exitCode, result.output)
        }
    }

    @Test
    fun `maximum response time gate fails one outlier when mean is within its limit`() {
        BasicHttpTestServer(postDelaysMs = listOf(0, 0, 250)).use { server ->
            val result =
                runSimulation(
                    server,
                    mapOf(
                        "gatling.basic.mode" to "gated",
                        "gatling.local.basic.meanResponseTimeMs" to "150",
                        "gatling.local.basic.maxResponseTimeMs" to "200",
                    ),
                )

            assertEquals(6, server.requests.size)
            assertEquals(2, result.exitCode, result.output)
        }
    }

    @Test
    fun `gated mode passes correct responses within explicit mean and maximum limits`() {
        BasicHttpTestServer().use { server ->
            val result =
                runSimulation(
                    server,
                    mapOf(
                        "gatling.basic.mode" to "gated",
                        "gatling.local.basic.meanResponseTimeMs" to "10000",
                        "gatling.local.basic.maxResponseTimeMs" to "10000",
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
                "gatling.basic.iterations" to "2",
                "gatling.basic.maxIterations" to "2",
            ) + extraProperties
        return GatlingTestRunner(outputDirectory).run(BasicContractSimulation::class.java, properties)
    }
}
