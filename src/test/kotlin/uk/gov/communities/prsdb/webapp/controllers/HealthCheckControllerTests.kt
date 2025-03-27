package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import kotlin.test.Test

@WebMvcTest(HealthCheckController::class)
class HealthCheckControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Test
    fun `HealthCheckController returns 200 unauthenticated user`() {
        mvc
            .get("/healthcheck")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `HealthCheckController returns 200 when trailing slash is included`() {
        mvc
            .get("/healthcheck/")
            .andExpect {
                status { isOk() }
            }
    }
}
