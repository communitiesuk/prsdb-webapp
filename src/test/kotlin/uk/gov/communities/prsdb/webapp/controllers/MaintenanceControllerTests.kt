package uk.gov.communities.prsdb.webapp.controllers

import org.hibernate.annotations.NotFound
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.controllers.MaintenanceController.Companion.MAINTENANCE_ROUTE

// The ActiveProfiles annotation is not picked up for inner classes, so we need to create separate classes for each profile
@WebMvcTest(MaintenanceController::class)
@ActiveProfiles("maintenance-mode")
class MaintenanceControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Test
    fun `index returns Ok for an unauthenticated user`() {
        mvc
            .get(MAINTENANCE_ROUTE)
            .andExpect {
                status { isOk() }
                view { name("maintenancePage") }
            }
    }

    @Test
    @WithMockUser
    fun `index returns Ok for an authenticated user`() {
        mvc
            .get(MAINTENANCE_ROUTE)
            .andExpect {
                status { isOk() }
                view { name("maintenancePage") }
            }
    }
}

@WebMvcTest(MaintenanceController::class)
class MaintenanceControllerWithoutMaintenanceModeTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Test
    fun `index returns Page Not Found`() {
        mvc
            .get(MAINTENANCE_ROUTE)
            .andExpect {
                status { NotFound() }
            }
    }
}
