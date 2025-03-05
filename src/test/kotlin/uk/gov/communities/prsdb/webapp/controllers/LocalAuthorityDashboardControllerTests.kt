package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityDashboardController.Companion.LOCAL_AUTHORITY_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthorityUser
import kotlin.test.Test

@WebMvcTest(LocalAuthorityDashboardController::class)
class LocalAuthorityDashboardControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockBean
    private lateinit var localAuthorityDataService: LocalAuthorityDataService

    @Test
    fun `index returns a redirect for unauthenticated user`() {
        mvc
            .get("/$LOCAL_AUTHORITY_PATH_SEGMENT")
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @WithMockUser
    @Test
    fun `index returns 403 for unauthorized user`() {
        mvc.get("/$LOCAL_AUTHORITY_PATH_SEGMENT").andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `index returns a redirect for authorised user`() {
        mvc.get("/$LOCAL_AUTHORITY_PATH_SEGMENT").andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    fun `localAuthorityDashboard returns a redirect for unauthenticated user`() {
        mvc.get(LOCAL_AUTHORITY_DASHBOARD_URL).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `localAuthorityDashboard returns 403 for unauthorized user`() {
        mvc
            .get(LOCAL_AUTHORITY_DASHBOARD_URL)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `localAuthorityDashboard returns 200 for authorised local authority admin user`() {
        val localAuthorityUser = createLocalAuthorityUser()
        whenever(localAuthorityDataService.getLocalAuthorityUser("user")).thenReturn(localAuthorityUser)

        mvc
            .get(LOCAL_AUTHORITY_DASHBOARD_URL)
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_USER"])
    fun `localAuthorityDashboard returns 200 for authorised local authority user`() {
        val localAuthorityUser = createLocalAuthorityUser()
        whenever(localAuthorityDataService.getLocalAuthorityUser("user")).thenReturn(localAuthorityUser)

        mvc
            .get(LOCAL_AUTHORITY_DASHBOARD_URL)
            .andExpect {
                status { isOk() }
            }
    }
}
