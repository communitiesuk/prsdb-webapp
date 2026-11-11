package uk.gov.communities.prsdb.webapp.local.api.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
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
    private val mvc = MockMvcBuilders.standaloneSetup(MockEpcRegisterController(service)).build()
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
                .with { it.remoteAddr = "192.0.2.1"; it }
                .header(HttpHeaders.AUTHORIZATION, credentials)
                .param("grant_type", "client_credentials"),
        ).andExpect(status().isForbidden)
        mvc.perform(
            post("$route/auth/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "client_credentials"),
        )
            .andExpect(status().isUnauthorized)
        mvc.perform(get("$route/api/prsdatabase/assessments/search").param("uprn", "UPRN-000000000901"))
            .andExpect(status().isUnauthorized)
        mvc.perform(
            get("$route/api/prsdatabase/assessments/search")
                .with { it.remoteAddr = "192.0.2.1"; it }
                .header(HttpHeaders.AUTHORIZATION, "Bearer performance-test-epc-token"),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `unknown and malformed queries return EPC compatible failures`() {
        whenever(service.search("UPRN-000000000901", null)).thenReturn(null)
        mvc.perform(
            get("$route/api/prsdatabase/assessments/search")
                .header(HttpHeaders.AUTHORIZATION, "Bearer performance-test-epc-token")
                .param("uprn", "UPRN-000000000901"),
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
    fun `stub is only registered in the NFT performance profile and never in production`() {
        for ((profiles, enabled) in listOf(
            arrayOf("nft") to false,
            arrayOf("performance-test") to false,
            arrayOf("nft", "performance-test") to true,
            arrayOf("nft", "performance-test", "production") to false,
        )) {
            AnnotationConfigApplicationContext().use { context ->
                context.environment.setActiveProfiles(*profiles)
                context.beanFactory.registerSingleton("performanceTestEpcService", service)
                context.register(MockEpcRegisterController::class.java)
                context.refresh()
                assertEquals(enabled, context.getBeansOfType(MockEpcRegisterController::class.java).isNotEmpty())
            }
        }
    }
}
