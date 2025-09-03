package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
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
            .get(HealthCheckController.HEALTHCHECK_ROUTE)
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `HealthCheckController returns 308 when trailing slash is included`() {
        mvc
            .get("${HealthCheckController.HEALTHCHECK_ROUTE}/")
            .andExpect {
                status { isPermanentRedirect() }
            }
    }
}

@WebMvcTest(HealthCheckController::class)
@ActiveProfiles("maintenance-mode")
class HealthCheckControllerInMaintenanceModeTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Test
    fun `HealthCheckController returns 200 in maintenance mode`() {
        mvc
            .get(HealthCheckController.HEALTHCHECK_ROUTE)
            .andExpect {
                status { isOk() }
            }
    }
}
