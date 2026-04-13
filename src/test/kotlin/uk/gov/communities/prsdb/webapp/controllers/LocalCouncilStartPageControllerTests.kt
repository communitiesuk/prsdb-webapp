package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import kotlin.test.Test

@WebMvcTest(LocalCouncilStartPageController::class)
class LocalCouncilStartPageControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Test
    fun `getStartPage returns 200 for unauthenticated user`() {
        mvc
            .get(LocalCouncilStartPageController.LOCAL_COUNCIL_START_PAGE_ROUTE)
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `getStartPage returns the localCouncilStartPage view`() {
        mvc
            .get(LocalCouncilStartPageController.LOCAL_COUNCIL_START_PAGE_ROUTE)
            .andExpect {
                view { name("localCouncilStartPage") }
            }
    }

    @Test
    fun `getStartPage sets localCouncilDashboardUrl model attribute`() {
        mvc
            .get(LocalCouncilStartPageController.LOCAL_COUNCIL_START_PAGE_ROUTE)
            .andExpect {
                model { attributeExists("localCouncilDashboardUrl") }
            }
    }
}
