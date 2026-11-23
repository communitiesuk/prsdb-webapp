package uk.gov.communities.prsdb.webapp.local.api.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.core.env.Profiles
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.env.MockEnvironment
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import uk.gov.communities.prsdb.webapp.services.PerformanceTestEpcService
import java.util.Base64
import kotlin.test.assertEquals

class MockEpcRegisterControllerTests {
    private val service: PerformanceTestEpcService = mock()
    private val mvc = MockMvcBuilders.standaloneSetup(MockEpcRegisterController(service, "performance-test", "performance-test")).build()
    private val route = "/local/epc"
    private val credentials = "Basic " + Base64.getEncoder().encodeToString("performance-test:performance-test".toByteArray())

    @Test
    fun `loopback client obtains a token and searches the controlled EPC API`() {
        mvc.perform(
            post("$route/auth/oauth/token")
                .header(HttpHeaders.AUTHORIZATION, credentials)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "client_credentials"),
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.access_token").value("performance-test-epc-token"))

        whenever(service.search("UPRN-000000000901", null)).thenReturn(mapOf("data" to mapOf("epcRrn" to "synthetic")))
        mvc.perform(
            get("$route/api/prsdatabase/assessments/search")
                .header(HttpHeaders.AUTHORIZATION, "Bearer performance-test-epc-token")
                .param("uprn", "UPRN-000000000901"),
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.epcRrn").value("synthetic"))
    }

    @Test
    fun `non-loopback callers and incorrect credentials cannot obtain tokens or query addresses`() {
        mvc.perform(
            post("$route/auth/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with {
                    it.remoteAddr = "192.0.2.1"
                    it
                }
                .header(HttpHeaders.AUTHORIZATION, credentials)
                .param("grant_type", "client_credentials"),
        ).andExpect(status().isForbidden)
        mvc.perform(
            post("$route/auth/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "client_credentials"),
        ).andExpect(status().isUnauthorized)
        mvc.perform(get("$route/api/prsdatabase/assessments/search").param("uprn", "UPRN-000000000901"))
            .andExpect(status().isUnauthorized)
        mvc.perform(
            get("$route/api/prsdatabase/assessments/search")
                .with {
                    it.remoteAddr = "192.0.2.1"
                    it
                }
                .header(HttpHeaders.AUTHORIZATION, "Bearer performance-test-epc-token"),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `not-found EPC variant returns the normalized EPC error and malformed queries are rejected`() {
        whenever(service.search(null, "0000-0000-0000-0000-9014")).thenReturn(null)
        mvc.perform(
            get("$route/api/prsdatabase/assessments/search")
                .header(HttpHeaders.AUTHORIZATION, "Bearer performance-test-epc-token")
                .param("rrn", "0000-0000-0000-0000-9014"),
        ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.errors[0].code").value("NOT_FOUND"))

        whenever(service.search(null, null)).thenThrow(IllegalArgumentException())
        mvc.perform(
            get("$route/api/prsdatabase/assessments/search")
                .header(HttpHeaders.AUTHORIZATION, "Bearer performance-test-epc-token"),
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors[0].code").value("INVALID_REQUEST"))
    }

    @Test
    fun `mock is available only in local or NFT performance profiles and never in production`() {
        for ((profiles, enabled) in listOf(
            arrayOf("nft") to false,
            arrayOf("local") to false,
            arrayOf("performance-test") to false,
            arrayOf("nft", "performance-test") to true,
            arrayOf("local", "performance-test") to true,
            arrayOf("nft", "performance-test", "production") to false,
            arrayOf("local", "performance-test", "production") to false,
        )) {
            AnnotationConfigApplicationContext().use { context ->
                context.environment.setActiveProfiles(*profiles)
                context.environment.propertySources.addFirst(
                    org.springframework.core.env.MapPropertySource(
                        "test-credentials",
                        mapOf(
                            "spring.security.oauth2.client.registration.epc-register.client-id" to "performance-test",
                            "spring.security.oauth2.client.registration.epc-register.client-secret" to "performance-test",
                        ),
                    ),
                )
                context.beanFactory.registerSingleton("performanceTestEpcService", service)
                context.register(MockEpcRegisterController::class.java)
                context.refresh()
                assertEquals(enabled, context.getBeansOfType(MockEpcRegisterController::class.java).isNotEmpty())
            }
        }
    }

    @Test
    fun `token endpoint uses configured EPC client credentials`() {
        AnnotationConfigApplicationContext().use { context ->
            context.environment.setActiveProfiles("nft", "performance-test")
            context.environment.propertySources.addFirst(
                org.springframework.core.env.MapPropertySource(
                    "test-credentials",
                    mapOf(
                        "spring.security.oauth2.client.registration.epc-register.client-id" to "custom-client",
                        "spring.security.oauth2.client.registration.epc-register.client-secret" to "custom-secret",
                    ),
                ),
            )
            context.beanFactory.registerSingleton("performanceTestEpcService", service)
            context.register(MockEpcRegisterController::class.java)
            context.refresh()
            val configuredMvc = MockMvcBuilders.standaloneSetup(context.getBean(MockEpcRegisterController::class.java)).build()
            val customCredentials = "Basic " + Base64.getEncoder().encodeToString("custom-client:custom-secret".toByteArray())
            for ((authorization, expected) in listOf(customCredentials to status().isOk, credentials to status().isUnauthorized)) {
                configuredMvc.perform(
                    post("$route/auth/oauth/token")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials"),
                ).andExpect(expected)
            }
        }
    }

    @Test
    fun `local EPC settings activate without enabling NFT internal access settings`() {
        val sources = YamlPropertySourceLoader().load("performance-test", ClassPathResource("application-performance-test.yml"))
        for ((profiles, epcExpected, internalAccessExpected) in listOf(
            Triple(arrayOf("local", "performance-test"), true, false),
            Triple(arrayOf("nft", "performance-test"), true, true),
            Triple(arrayOf("local"), false, false),
            Triple(arrayOf("local", "performance-test", "production"), false, false),
            Triple(arrayOf("nft", "performance-test", "production"), false, false),
        )) {
            val environment = MockEnvironment()
            environment.setActiveProfiles(*profiles)
            sources.filter {
                environment.acceptsProfiles(Profiles.of(it.getProperty("spring.config.activate.on-profile") as String))
            }.forEach { environment.propertySources.addFirst(it) }
            assertEquals(epcExpected, environment.containsProperty("epc.base-url"))
            assertEquals(internalAccessExpected, environment.containsProperty("performance-test.internal-access.client-secret"))
        }
    }

    @Test
    fun `local performance profile overrides local staging EPC configuration`() {
        val environment =
            MockEnvironment().withProperty("spring.config.location", "file:src/main/resources/")
        environment.setActiveProfiles("local", "local-no-auth", "performance-test")
        ConfigDataEnvironmentPostProcessor.applyTo(environment)

        assertEquals(
            "performance-test",
            environment.getRequiredProperty("spring.security.oauth2.client.registration.epc-register.client-id"),
        )
        assertEquals(
            "performance-test",
            environment.getRequiredProperty("spring.security.oauth2.client.registration.epc-register.client-secret"),
        )
        assertEquals(
            "http://localhost:8080/local/epc/auth/oauth/token",
            environment.getRequiredProperty("spring.security.oauth2.client.provider.epc-register.token-uri"),
        )
        assertEquals("http://localhost:8080/local/epc", environment.getRequiredProperty("epc.base-url"))
    }

    @Test
    fun `payment mock URL defaults to local host but respects explicit override`() {
        val environment = MockEnvironment()
        YamlPropertySourceLoader().load("performance-test", ClassPathResource("application-performance-test.yml"))
            .forEach { environment.propertySources.addFirst(it) }
        assertEquals("http://localhost:8080/local/gov-uk-pay", environment.getRequiredProperty("gov-uk-pay.mock-base-url"))
        environment.setProperty("server.port", "9090")
        assertEquals("http://localhost:9090/local/gov-uk-pay", environment.getRequiredProperty("gov-uk-pay.mock-base-url"))
        environment.setProperty("GOV_UK_PAY_MOCK_BASE_URL", "https://nft.example.test/local/gov-uk-pay")
        assertEquals("https://nft.example.test/local/gov-uk-pay", environment.getRequiredProperty("gov-uk-pay.mock-base-url"))
    }

    @Test
    fun `performance mock URLs default to port 8080 and respect an explicit port`() {
        val environment =
            MockEnvironment().withProperty(
                "GOV_UK_PAY_MOCK_BASE_URL",
                "https://nft.example.test/local/gov-uk-pay",
            )
        YamlPropertySourceLoader().load("performance-test", ClassPathResource("application-performance-test.yml"))
            .forEach { environment.propertySources.addFirst(it) }
        val paths =
            mapOf(
                "gov-uk-pay.base-url" to "/local/gov-uk-pay",
                "gov-uk-pay.mock-base-url" to "/local/gov-uk-pay",
                "epc.base-url" to "/local/epc",
                "epc.certificate-base-url" to "/local/epc/energy-certificate",
                "spring.security.oauth2.client.provider.epc-register.token-uri" to "/local/epc/auth/oauth/token",
            )

        assertEquals("https://nft.example.test/local/gov-uk-pay", environment.getRequiredProperty("gov-uk-pay.mock-base-url"))
        for ((key, path) in paths.filterKeys { it != "gov-uk-pay.mock-base-url" }) {
            assertEquals("http://localhost:8080$path", environment.getRequiredProperty(key))
        }
        environment.setProperty("server.port", "9090")
        for ((key, path) in paths.filterKeys { it != "gov-uk-pay.mock-base-url" }) {
            assertEquals("http://localhost:9090$path", environment.getRequiredProperty(key))
        }
    }
}
