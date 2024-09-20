package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import kotlin.test.Test

@WebMvcTest(RegisterHomeController::class)
class RegisterHomeControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Test
    fun `RegisterHomeController returns a redirect for unauthenticated user`() {
        mvc.get("/registration").andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `RegisterHomeController returns 403 for unauthorized user`() {
        mvc
            .get("/registration")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `RegisterHomeController returns 200 for authorized user`() {
        mvc
            .get("/registration")
            .andExpect {
                status { isOk() }
            }
    }
}
