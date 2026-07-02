package uk.gov.communities.prsdb.webapp.controllers

import org.hamcrest.Matchers
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_DASHBOARD_SURVEY_URL
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

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
    fun `localCouncilDashboard includes the dashboard survey URL in the model`() {
        val localCouncilUser = createLocalCouncilUser()
        whenever(localCouncilDataService.getLocalCouncilUser("user")).thenReturn(localCouncilUser)

        mvc
            .get(LOCAL_COUNCIL_DASHBOARD_URL)
            .andExpect {
                status { isOk() }
                model { attribute("localCouncilDashboardSurveyUrl", LOCAL_COUNCIL_DASHBOARD_SURVEY_URL) }
            }
    }

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
    fun `localCouncilDashboard sets isLocalCouncilAdmin to true for an admin user`() {
        val localCouncilUser = createLocalCouncilUser()
        whenever(localCouncilDataService.getLocalCouncilUser("user")).thenReturn(localCouncilUser)
        whenever(userRolesService.getHasLocalCouncilAdminRole("user")).thenReturn(true)

        mvc
            .get(LOCAL_COUNCIL_DASHBOARD_URL)
            .andExpect {
                status { isOk() }
                model { attribute("isLocalCouncilAdmin", true) }
            }
    }

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
    fun `localCouncilDashboard shows the dashboard nav link first then manage users for an admin`() {
        val localCouncilUser = createLocalCouncilUser()
        whenever(localCouncilDataService.getLocalCouncilUser("user")).thenReturn(localCouncilUser)
        whenever(userRolesService.getHasLocalCouncilAdminRole("user")).thenReturn(true)

        mvc
            .get(LOCAL_COUNCIL_DASHBOARD_URL)
            .andExpect {
                status { isOk() }
                model {
                    attribute(
                        "navLinks",
                        Matchers.contains(
                            Matchers.hasProperty<Any>("href", Matchers.equalTo(LOCAL_COUNCIL_DASHBOARD_URL)),
                            Matchers.hasProperty<Any>("messageProperty", Matchers.equalTo("navLink.manageUsers.title")),
                        ),
                    )
                }
            }
    }

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
    fun `localCouncilDashboard shows only the dashboard nav link for a non-admin user`() {
        val localCouncilUser = createLocalCouncilUser()
        whenever(localCouncilDataService.getLocalCouncilUser("user")).thenReturn(localCouncilUser)
        whenever(userRolesService.getHasLocalCouncilAdminRole("user")).thenReturn(false)

        mvc
            .get(LOCAL_COUNCIL_DASHBOARD_URL)
            .andExpect {
                status { isOk() }
                model {
                    attribute(
                        "navLinks",
                        Matchers.contains(
                            Matchers.hasProperty<Any>("href", Matchers.equalTo(LOCAL_COUNCIL_DASHBOARD_URL)),
                        ),
                    )
                }
            }
    }
}
