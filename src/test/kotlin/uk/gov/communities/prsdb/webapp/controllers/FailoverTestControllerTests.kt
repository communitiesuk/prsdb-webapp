package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(FailoverTestController::class)
class FailoverTestControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @WithMockUser
    @Test
    fun `failover endpoints return the correct status codes`() {
        val statusCodes = listOf(501, 502, 503, 504)
        val urlRoutes =
            listOf(
                FailoverTestController.ERROR_501_URL_ROUTE,
                FailoverTestController.ERROR_502_URL_ROUTE,
                FailoverTestController.ERROR_503_URL_ROUTE,
                FailoverTestController.ERROR_504_URL_ROUTE,
            )

        for (i in statusCodes.indices) {
            mvc
                .get(urlRoutes[i])
                .andExpect { status { isEqualTo(statusCodes[i]) } }
        }
    }
}
