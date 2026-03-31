package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilStartPageController.Companion.LOCAL_COUNCIL_START_PAGE_ROUTE

@WebMvcTest(LocalCouncilStartPageController::class)
class LocalCouncilStartPageControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Test
    fun `index returns 200 for unauthenticated user`() {
        mvc.get(LOCAL_COUNCIL_START_PAGE_ROUTE).andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `index returns 308 for unauthenticated user with trailing slash`() {
        mvc.get("$LOCAL_COUNCIL_START_PAGE_ROUTE/").andExpect {
            status { isPermanentRedirect() }
        }
    }
}
