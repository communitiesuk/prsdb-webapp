package uk.gov.communities.prsdb.webapp.performance

import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BasicRunConfigTests {
    private val localProperties =
        mapOf(
            "gatling.target" to "local",
            "gatling.baseUrl" to "http://localhost:8080",
            "gatling.simulatorUrl" to "http://localhost:3000",
            "gatling.basic.landlordSubject" to "synthetic-landlord",
            "gatling.basic.mode" to "baseline",
        )

    @Test
    fun `council simulator cannot reuse One Login or send NFT authentication to loopback`() {
        for (invalid in listOf("http://localhost:3000", "http://localhost:3001", "https://council.example.test/path")) {
            assertFailsWith<IllegalArgumentException> {
                BasicRunConfig.from(
                    localProperties +
                        mapOf(
                            "gatling.target" to "nft",
                            "gatling.council.simulatorUrl" to invalid,
                        ),
                )
            }
        }
    }

    @Test
    fun `gated mode rejects missing response time limits`() {
        val properties =
            localProperties +
                mapOf(
                    "gatling.basic.mode" to "gated",
                    "gatling.local.basic.meanResponseTimeMs" to "1000",
                )
        val exception = assertFailsWith<IllegalArgumentException> { BasicRunConfig.from(properties) }

        assertTrue(exception.message.orEmpty().contains("maxResponseTimeMs"))
    }

    @Test
    fun `baseline mode keeps functional configuration without timing limits`() {
        val config = BasicRunConfig.from(localProperties)

        assertEquals(PerformanceTarget.LOCAL, config.target)
        assertEquals(MeasurementMode.BASELINE, config.mode)
        assertEquals("http://localhost:8080", config.baseUrl)
        assertEquals("http://localhost:3000", config.simulatorUrl)
        assertEquals("synthetic-landlord", config.landlordSubject)
        assertNull(config.limits)
    }

    @Test
    fun `NFT certificate origin must be a separate HTTPS S3 bucket origin`() {
        val origin = "https://prsdb-uploaded-files-nft.s3.eu-west-2.amazonaws.com"
        val properties =
            localProperties +
                mapOf(
                    "gatling.target" to "nft",
                    "gatling.baseUrl" to "https://app.example.test",
                )
        assertEquals(
            origin,
            BasicRunConfig.from(properties + ("gatling.certificate.downloadOrigin" to origin)).certificateDownloadOrigin,
        )
        for (invalid in listOf(
            origin.replace("https:", "http:"),
            "$origin:443",
            "$origin/certificate.pdf",
            "$origin?signature=secret",
            "https://app.example.test",
            "https://other.example.test",
        )) {
            assertFailsWith<IllegalArgumentException> {
                BasicRunConfig.from(properties + ("gatling.certificate.downloadOrigin" to invalid))
            }
        }
    }

    @Test
    fun `workflow ramp supplies all mandatory basic configuration`() {
        val command =
            Files.readString(Path.of(".github/workflows/run-performance-tests.yml"))
                .substringAfter("--simulation ${BaselineLoadSimulation::class.java.name}")
                .substringBefore("\n      - name:")
        val arguments =
            command.lineSequence().map { it.trim().removeSuffix("\\").trim() }
                .filter { it.startsWith("-Dgatling.") }
                .associate {
                    val (key, value) = it.removePrefix("-D").split("=", limit = 2)
                    key to
                        value.removeSurrounding("\"").replace(
                            "\$COUNCIL_SIMULATOR_URL",
                            "https://council-simulator.example.test",
                        )
                }
        val config = BasicRunConfig.from(arguments)
        assertEquals(PerformanceTarget.NFT, config.target)
        assertTrue(config.landlordSubject.isNotBlank())
        val rampSeconds = arguments.getValue("gatling.ramp.rampMinutes").toDouble() * 60
        val holdSeconds = arguments.getValue("gatling.ramp.holdMinutes").toDouble() * 60
        val registrations =
            RampSchedule(arguments.getValue("gatling.ramp.registrationsPerMinute").toDouble(), rampSeconds, holdSeconds)
                .arrivalTimesMillis()
        val updates =
            RampSchedule(arguments.getValue("gatling.ramp.updatesPerMinute").toDouble(), rampSeconds, holdSeconds)
                .arrivalTimesMillis()
        val offset = arguments.getValue("gatling.ramp.fixtureOffset").toInt()
        assertTrue(offset >= 2 * (config.iterations + 1), "Ramp must skip property-update and certificate correctness allocations")
        assertTrue(offset + updates.size <= 101, "Ramp must fit each exported fixture pool")
        assertEquals(665, registrations.size + 3 * updates.size)
        assertTrue("gatling.registration.paymentBaseUrl" !in arguments, "Baseline must not require the unfinished payment ingress")
    }

    @Test
    fun `gated mode uses explicit local limits`() {
        val config =
            BasicRunConfig.from(
                localProperties +
                    mapOf(
                        "gatling.basic.mode" to "gated",
                        "gatling.local.basic.meanResponseTimeMs" to "1000",
                        "gatling.local.basic.maxResponseTimeMs" to "2000",
                    ),
            )

        assertEquals(ResponseTimeLimits(meanMs = 1000, maxMs = 2000), config.limits)
    }

    @Test
    fun `NFT never inherits local response time limits`() {
        val properties =
            localProperties +
                mapOf(
                    "gatling.target" to "nft",
                    "gatling.basic.mode" to "gated",
                    "gatling.nft.basic.meanResponseTimeMs" to "1000",
                    "gatling.local.basic.maxResponseTimeMs" to "2000",
                )

        val exception = assertFailsWith<IllegalArgumentException> { BasicRunConfig.from(properties) }

        assertTrue(exception.message.orEmpty().contains("gatling.nft.basic.maxResponseTimeMs"))
    }

    @Test
    fun `NFT uses its own limits and independently supplied URLs`() {
        val config =
            BasicRunConfig.from(
                localProperties +
                    mapOf(
                        "gatling.target" to "nft",
                        "gatling.baseUrl" to "https://app.example.test",
                        "gatling.simulatorUrl" to "https://simulator.example.test",
                        "gatling.basic.mode" to "gated",
                        "gatling.nft.basic.meanResponseTimeMs" to "2000",
                        "gatling.nft.basic.maxResponseTimeMs" to "4000",
                    ),
            )

        assertEquals(PerformanceTarget.NFT, config.target)
        assertEquals("https://app.example.test", config.baseUrl)
        assertEquals(ResponseTimeLimits(meanMs = 2000, maxMs = 4000), config.limits)
    }

    @Test
    fun `target mode URLs and subject must be explicitly provided`() {
        localProperties.keys.forEach { key ->
            val exception = assertFailsWith<IllegalArgumentException> { BasicRunConfig.from(localProperties - key) }
            assertTrue(exception.message.orEmpty().contains(key), "Missing property must be named: $key")
        }
    }

    @Test
    fun `unknown target and measurement mode fail instead of falling back`() {
        listOf("gatling.target", "gatling.basic.mode").forEach { key ->
            assertFailsWith<IllegalArgumentException> { BasicRunConfig.from(localProperties + (key to "unknown")) }
        }
    }

    @Test
    fun `blank required properties fail with the property name`() {
        localProperties.keys.forEach { key ->
            val exception = assertFailsWith<IllegalArgumentException> { BasicRunConfig.from(localProperties + (key to " ")) }
            assertTrue(exception.message.orEmpty().contains(key))
        }
    }

    @Test
    fun `malformed URLs unsupported schemes credentials and query strings are rejected`() {
        val invalidUrls =
            listOf(
                "not a URL",
                "ftp://localhost:8080",
                "http:/localhost",
                "http://user:password@localhost:8080",
                "http://localhost:8080?token=secret",
                "http://localhost:8080#fragment",
                "http://localhost:8080/path",
                "http://localhost:0",
                "http://localhost:65536",
            )

        listOf("gatling.baseUrl", "gatling.simulatorUrl").forEach { key ->
            invalidUrls.forEach { value ->
                val exception = assertFailsWith<IllegalArgumentException> { BasicRunConfig.from(localProperties + (key to value)) }
                assertTrue(exception.message.orEmpty().contains(key))
                assertTrue(!exception.message.orEmpty().contains("password"), "Errors must not expose URL credentials")
            }
        }
    }

    @Test
    fun `trailing slashes are normalised and worktree ports are preserved`() {
        val config =
            BasicRunConfig.from(
                localProperties +
                    mapOf(
                        "gatling.baseUrl" to "http://127.0.0.1:8091/",
                        "gatling.simulatorUrl" to "http://127.0.0.1:3011/",
                    ),
            )

        assertEquals("http://127.0.0.1:8091", config.baseUrl)
        assertEquals("http://127.0.0.1:3011", config.simulatorUrl)
    }

    @Test
    fun `timing limits must be positive integer milliseconds`() {
        listOf("0", "-1", "NaN", "Infinity", "1.5", "2147483648", " ").forEach { value ->
            val properties =
                localProperties +
                    mapOf(
                        "gatling.basic.mode" to "gated",
                        "gatling.local.basic.meanResponseTimeMs" to value,
                        "gatling.local.basic.maxResponseTimeMs" to value,
                    )
            assertFailsWith<IllegalArgumentException> { BasicRunConfig.from(properties) }
        }
    }

    @Test
    fun `gated mode requires the mean limit`() {
        val properties =
            localProperties +
                mapOf(
                    "gatling.basic.mode" to "gated",
                    "gatling.local.basic.maxResponseTimeMs" to "2000",
                )

        val exception = assertFailsWith<IllegalArgumentException> { BasicRunConfig.from(properties) }

        assertTrue(exception.message.orEmpty().contains("meanResponseTimeMs"))
    }

    @Test
    fun `gated mode requires the maximum limit`() {
        val properties =
            localProperties +
                mapOf(
                    "gatling.basic.mode" to "gated",
                    "gatling.local.basic.meanResponseTimeMs" to "1000",
                )

        val exception = assertFailsWith<IllegalArgumentException> { BasicRunConfig.from(properties) }

        assertTrue(exception.message.orEmpty().contains("maxResponseTimeMs"))
    }

    @Test
    fun `gated mode accepts equal mean and maximum thresholds`() {
        val config =
            BasicRunConfig.from(
                localProperties +
                    mapOf(
                        "gatling.basic.mode" to "gated",
                        "gatling.local.basic.meanResponseTimeMs" to "2000",
                        "gatling.local.basic.maxResponseTimeMs" to "2000",
                    ),
            )

        assertEquals(ResponseTimeLimits(meanMs = 2000, maxMs = 2000), config.limits)
    }

    @Test
    fun `gated mode rejects a maximum threshold lower than the mean`() {
        val properties =
            localProperties +
                mapOf(
                    "gatling.basic.mode" to "gated",
                    "gatling.local.basic.meanResponseTimeMs" to "2001",
                    "gatling.local.basic.maxResponseTimeMs" to "2000",
                )

        val exception = assertFailsWith<IllegalArgumentException> { BasicRunConfig.from(properties) }

        assertTrue(exception.message.orEmpty().contains("maxResponseTimeMs"))
    }

    @Test
    fun `baseline mode rejects supplied timing limits rather than silently ignoring them`() {
        val properties = localProperties + ("gatling.local.basic.maxResponseTimeMs" to "2000")

        assertFailsWith<IllegalArgumentException> { BasicRunConfig.from(properties) }
    }

    @Test
    fun `baseline mode rejects a supplied mean limit`() {
        val properties = localProperties + ("gatling.local.basic.meanResponseTimeMs" to "1000")

        assertFailsWith<IllegalArgumentException> { BasicRunConfig.from(properties) }
    }

    @Test
    fun `iteration settings default to ten with a configurable ceiling of one hundred`() {
        val defaults = BasicRunConfig.from(localProperties)
        val configured =
            BasicRunConfig.from(
                localProperties +
                    mapOf(
                        "gatling.basic.iterations" to "20",
                        "gatling.basic.maxIterations" to "25",
                    ),
            )

        assertEquals(10, defaults.iterations)
        assertEquals(100, defaults.maxIterations)
        assertEquals(20, configured.iterations)
        assertEquals(25, configured.maxIterations)
    }

    @Test
    fun `invalid iteration count and ceiling values are rejected`() {
        listOf("0", "-1", "not-a-number").forEach { value ->
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    BasicRunConfig.from(localProperties + ("gatling.basic.iterations" to value))
                }
            assertTrue(exception.message.orEmpty().contains("gatling.basic.iterations"))
        }

        listOf("0", "-1", "not-a-number").forEach { value ->
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    BasicRunConfig.from(localProperties + ("gatling.basic.maxIterations" to value))
                }
            assertTrue(exception.message.orEmpty().contains("gatling.basic.maxIterations"))
        }
    }

    @Test
    fun `iteration count above configurable ceiling is rejected`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                BasicRunConfig.from(
                    localProperties +
                        mapOf(
                            "gatling.basic.iterations" to "11",
                            "gatling.basic.maxIterations" to "10",
                        ),
                )
            }

        assertTrue(exception.message.orEmpty().contains("gatling.basic.maxIterations"))
    }
}
