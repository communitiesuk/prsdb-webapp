package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.controllers.CookiesController.Companion.COOKIES_ROUTE

@WebMvcTest(CookiesController::class)
class CookiesControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Test
    fun `getCookiesPage returns 200 for unauthenticated users`() {
        mvc
            .get(COOKIES_ROUTE)
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser
    fun `getCookiesPage returns 200 for authenticated users`() {
        mvc
            .get(COOKIES_ROUTE)
            .andExpect {
                status { isOk() }
            }
    }
}
