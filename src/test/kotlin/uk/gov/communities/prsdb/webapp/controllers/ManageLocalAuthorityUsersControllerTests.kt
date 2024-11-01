package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import kotlin.test.Test

@WebMvcTest(ManageLocalAuthorityUsersController::class)
class ManageLocalAuthorityUsersControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockBean
    private lateinit var localAuthorityDataService: LocalAuthorityDataService

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
        val localAuthority = LocalAuthority()
        ReflectionTestUtils.setField(localAuthority, "id", 123)
        ReflectionTestUtils.setField(localAuthority, "name", "Test Local Authority")
        whenever(localAuthorityDataService.getLocalAuthorityForUser("user"))
            .thenReturn(localAuthority)
        whenever(localAuthorityDataService.getPaginatedUsersAndInvitations(localAuthority, 0))
            .thenReturn(PageImpl(listOf(), PageRequest.of(0, 10), 0))

        mvc
            .get("/manage-users")
            .andExpect {
                status { isOk() }
            }
    }
}
