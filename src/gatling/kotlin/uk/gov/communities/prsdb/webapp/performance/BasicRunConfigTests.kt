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
    private val nftProperties =
        localProperties +
            mapOf(
                "gatling.target" to "nft",
                "gatling.baseUrl" to "https://nft.register-home-to-rent.test.communities.gov.uk",
                "gatling.simulatorUrl" to "https://nft.lb.register-home-to-rent.test.communities.gov.uk",
            )

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
        assertEquals(config.baseUrl, config.councilBaseUrl)
        assertEquals("http://localhost:3000", config.simulatorUrl)
        assertEquals("synthetic-landlord", config.landlordSubject)
        assertNull(config.limits)
    }

    @Test
    fun `NFT certificate origin must be a separate HTTPS S3 bucket origin`() {
        val origin = "https://prsdb-uploaded-files-nft.s3.eu-west-2.amazonaws.com"
        val properties = nftProperties
        assertEquals(
            origin,
            BasicRunConfig.from(properties + ("gatling.certificate.downloadOrigin" to origin)).certificateDownloadOrigin,
        )
        for (invalid in listOf(
            origin.replace("https:", "http:"),
            "$origin:443",
            "$origin/certificate.pdf",
            "$origin?signature=secret",
            properties.getValue("gatling.baseUrl"),
            "https://other.example.test",
        )) {
            assertFailsWith<IllegalArgumentException> {
                BasicRunConfig.from(properties + ("gatling.certificate.downloadOrigin" to invalid))
            }
        }
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
            nftProperties +
                mapOf(
                    "gatling.basic.mode" to "gated",
                    "gatling.nft.basic.meanResponseTimeMs" to "1000",
                    "gatling.local.basic.maxResponseTimeMs" to "2000",
                )

        val exception = assertFailsWith<IllegalArgumentException> { BasicRunConfig.from(properties) }

        assertTrue(exception.message.orEmpty().contains("gatling.nft.basic.maxResponseTimeMs"))
    }

    @Test
    fun `NFT uses its own limits and approved origins`() {
        val config =
            BasicRunConfig.from(
                nftProperties +
                    mapOf(
                        "gatling.basic.mode" to "gated",
                        "gatling.nft.basic.meanResponseTimeMs" to "2000",
                        "gatling.nft.basic.maxResponseTimeMs" to "4000",
                    ),
            )

        assertEquals(PerformanceTarget.NFT, config.target)
        assertEquals(nftProperties.getValue("gatling.baseUrl"), config.baseUrl)
        assertEquals("https://nft.search-landlord-home-information.test.communities.gov.uk", config.councilBaseUrl)
        assertEquals(ResponseTimeLimits(meanMs = 2000, maxMs = 4000), config.limits)
    }

    @Test
    fun `NFT council origin is separately validated against the approved alias`() {
        val origin = "https://nft.search-landlord-home-information.test.communities.gov.uk"
        assertEquals(origin, BasicRunConfig.from(nftProperties + ("gatling.council.baseUrl" to origin)).councilBaseUrl)

        for (invalid in listOf(
            "https://unapproved.example.test",
            "http://localhost:8080",
            "https://user:password@nft.search-landlord-home-information.test.communities.gov.uk",
            "$origin/local-council",
        )) {
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    BasicRunConfig.from(nftProperties + ("gatling.council.baseUrl" to invalid))
                }
            assertTrue(exception.message.orEmpty().contains("gatling.council.baseUrl"))
            assertTrue(!exception.message.orEmpty().contains(invalid), "Errors must not echo destination URLs")
        }
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

        listOf("gatling.baseUrl", "gatling.simulatorUrl", "gatling.council.baseUrl").forEach { key ->
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
    fun `local workloads reject non-loopback application and simulator origins`() {
        for (key in listOf("gatling.baseUrl", "gatling.simulatorUrl")) {
            for (origin in listOf(
                "https://unapproved.example.test",
                "http://localhost.example.test:8080",
                nftProperties.getValue(key),
            )) {
                val exception =
                    assertFailsWith<IllegalArgumentException> {
                        BasicRunConfig.from(localProperties + (key to origin))
                    }
                assertTrue(exception.message.orEmpty().contains(key))
                assertTrue(!exception.message.orEmpty().contains(origin), "Errors must not echo destination URLs")
            }
        }
    }

    @Test
    fun `NFT workloads reject other environments local hosts and swapped application simulator origins`() {
        for (key in listOf("gatling.baseUrl", "gatling.simulatorUrl")) {
            val otherKey = if (key == "gatling.baseUrl") "gatling.simulatorUrl" else "gatling.baseUrl"
            for (origin in listOf(
                "https://unapproved.example.test",
                localProperties.getValue(key),
                nftProperties.getValue(otherKey),
                nftProperties.getValue(key).replace("https:", "http:"),
            )) {
                val exception =
                    assertFailsWith<IllegalArgumentException> {
                        BasicRunConfig.from(nftProperties + (key to origin))
                    }
                assertTrue(exception.message.orEmpty().contains(key))
                assertTrue(!exception.message.orEmpty().contains(origin), "Errors must not echo destination URLs")
            }
        }
    }

    @Test
    fun `local IPv6 loopback supports separate worktree ports`() {
        val config =
            BasicRunConfig.from(
                localProperties +
                    mapOf(
                        "gatling.baseUrl" to "http://[::1]:8091",
                        "gatling.simulatorUrl" to "http://[::1]:3011",
                    ),
            )

        assertEquals("http://[::1]:8091", config.baseUrl)
        assertEquals("http://[::1]:3011", config.simulatorUrl)
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
