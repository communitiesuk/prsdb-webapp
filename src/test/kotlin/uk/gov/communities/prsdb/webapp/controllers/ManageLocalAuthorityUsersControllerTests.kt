package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import kotlin.test.Test

@WebMvcTest(ManageLocalAuthorityUsersController::class)
class ManageLocalAuthorityUsersControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Test
    fun `ManageLocalAuthorityUsersController returns a redirect for unauthenticated user`() {
        mvc.get("/manage-users").andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `ManageLocalAuthorityUsersController returns 403 for unauthorized user`() {
        mvc
            .get("/manage-users")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `ManageLocalAuthorityUsersController returns 200 for authorized user`() {
        mvc
            .get("/manage-users")
            .andExpect {
                status { isOk() }
            }
    }
}
