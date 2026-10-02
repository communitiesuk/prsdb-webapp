package uk.gov.communities.prsdb.webapp.performance

import org.junit.jupiter.api.Test
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
    fun `gated mode rejects missing response time limits`() {
        val properties = localProperties + ("gatling.basic.mode" to "gated")
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
    fun `gated mode uses explicit local limits`() {
        val config =
            BasicRunConfig.from(
                localProperties +
                    mapOf(
                        "gatling.basic.mode" to "gated",
                        "gatling.local.basic.maxResponseTimeMs" to "2000",
                        "gatling.local.basic.meanResponseTimeMs" to "1000",
                    ),
            )

        assertEquals(ResponseTimeLimits(2000, 1000), config.limits)
    }

    @Test
    fun `NFT never inherits local response time limits`() {
        val properties =
            localProperties +
                mapOf(
                    "gatling.target" to "nft",
                    "gatling.basic.mode" to "gated",
                    "gatling.local.basic.maxResponseTimeMs" to "2000",
                    "gatling.local.basic.meanResponseTimeMs" to "1000",
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
                        "gatling.nft.basic.maxResponseTimeMs" to "4000",
                        "gatling.nft.basic.meanResponseTimeMs" to "1500",
                    ),
            )

        assertEquals(PerformanceTarget.NFT, config.target)
        assertEquals("https://app.example.test", config.baseUrl)
        assertEquals(ResponseTimeLimits(4000, 1500), config.limits)
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
                        "gatling.local.basic.maxResponseTimeMs" to value,
                        "gatling.local.basic.meanResponseTimeMs" to "1",
                    )
            assertFailsWith<IllegalArgumentException> { BasicRunConfig.from(properties) }
        }
    }

    @Test
    fun `gated mode requires both maximum and mean limits`() {
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
    fun `mean response time limit cannot exceed maximum`() {
        val properties =
            localProperties +
                mapOf(
                    "gatling.basic.mode" to "gated",
                    "gatling.local.basic.maxResponseTimeMs" to "1000",
                    "gatling.local.basic.meanResponseTimeMs" to "2000",
                )

        assertFailsWith<IllegalArgumentException> { BasicRunConfig.from(properties) }
    }

    @Test
    fun `baseline mode rejects supplied timing limits rather than silently ignoring them`() {
        val properties = localProperties + ("gatling.local.basic.maxResponseTimeMs" to "2000")

        assertFailsWith<IllegalArgumentException> { BasicRunConfig.from(properties) }
    }
}
