package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilDashboardController.Companion.LOCAL_COUNCIL_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.services.LocalCouncilDataService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.createLocalCouncilUser
import kotlin.test.Test

@WebMvcTest(LocalCouncilDashboardController::class)
class LocalCouncilDashboardControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var localCouncilDataService: LocalCouncilDataService

    @Test
    fun `index returns a redirect for unauthenticated user`() {
        mvc
            .get("/$LOCAL_COUNCIL_PATH_SEGMENT")
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @WithMockUser
    @Test
    fun `index returns 403 for unauthorized user`() {
        mvc.get("/$LOCAL_COUNCIL_PATH_SEGMENT").andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
    fun `index returns a redirect for authorised user`() {
        mvc.get("/$LOCAL_COUNCIL_PATH_SEGMENT").andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    fun `localCouncilDashboard returns a redirect for unauthenticated user`() {
        mvc.get(LOCAL_COUNCIL_DASHBOARD_URL).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `localCouncilDashboard returns 403 for unauthorized user`() {
        mvc
            .get(LOCAL_COUNCIL_DASHBOARD_URL)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
    fun `localCouncilDashboard returns 200 for authorised local council admin user`() {
        val localCouncilUser = createLocalCouncilUser()
        whenever(localCouncilDataService.getLocalCouncilUser("user")).thenReturn(localCouncilUser)

        mvc
            .get(LOCAL_COUNCIL_DASHBOARD_URL)
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
    fun `localCouncilDashboard returns 200 for authorised local council user`() {
        val localCouncilUser = createLocalCouncilUser()
        whenever(localCouncilDataService.getLocalCouncilUser("user")).thenReturn(localCouncilUser)

        mvc
            .get(LOCAL_COUNCIL_DASHBOARD_URL)
            .andExpect {
                status { isOk() }
            }
    }
}
