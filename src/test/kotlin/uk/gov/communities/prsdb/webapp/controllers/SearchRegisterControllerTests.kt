package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import kotlin.test.Test

@WebMvcTest(SearchRegisterController::class)
class SearchRegisterControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Test
    fun `SearchRegisterController returns a redirect for unauthenticated user`() {
        mvc.get("/search/landlord").andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `SearchRegisterController returns 403 for unauthorized user`() {
        mvc
            .get("/search/landlord")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_USER"])
    fun `SearchRegisterController returns 200 for authorized user`() {
        mvc
            .get("/search/landlord")
            .andExpect {
                status { isOk() }
            }
    }
}
