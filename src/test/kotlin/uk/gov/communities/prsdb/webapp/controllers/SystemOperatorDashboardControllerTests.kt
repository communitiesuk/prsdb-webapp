package uk.gov.communities.prsdb.webapp.controllers

import org.hamcrest.Matchers.containsString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.MessageSourceConfig
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.GeneratePassphraseController.Companion.GENERATE_PASSPHRASE_URL
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilAdminsController.Companion.INVITE_LOCAL_COUNCIL_ADMIN_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.MetricsController.Companion.METRICS_URL
import uk.gov.communities.prsdb.webapp.controllers.SystemOperatorDashboardController.Companion.SYSTEM_OPERATOR_DASHBOARD_URL
import kotlin.test.Test

@WebMvcTest(SystemOperatorDashboardController::class)
@Import(MessageSourceConfig::class)
class SystemOperatorDashboardControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Test
    fun `index returns a redirect for unauthenticated user`() {
        mvc.get("/$SYSTEM_OPERATOR_PATH_SEGMENT").andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `index returns 403 for unauthorized user`() {
        mvc.get("/$SYSTEM_OPERATOR_PATH_SEGMENT").andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `index returns a redirect for authorised system operator`() {
        mvc.get("/$SYSTEM_OPERATOR_PATH_SEGMENT").andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    fun `systemOperatorDashboard returns a redirect for unauthenticated user`() {
        mvc.get(SYSTEM_OPERATOR_DASHBOARD_URL).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `systemOperatorDashboard returns 403 for unauthorized user`() {
        mvc.get(SYSTEM_OPERATOR_DASHBOARD_URL).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `systemOperatorDashboard returns 403 for a landlord`() {
        mvc.get(SYSTEM_OPERATOR_DASHBOARD_URL).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
    fun `systemOperatorDashboard returns 403 for a local council user`() {
        mvc.get(SYSTEM_OPERATOR_DASHBOARD_URL).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
    fun `systemOperatorDashboard returns 403 for a local council admin`() {
        mvc.get(SYSTEM_OPERATOR_DASHBOARD_URL).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `systemOperatorDashboard returns 200 and the dashboard view for a system operator`() {
        mvc.get(SYSTEM_OPERATOR_DASHBOARD_URL).andExpect {
            status { isOk() }
            view { name("systemOperatorDashboard") }
            model {
                attribute("generatePassphraseUrl", GENERATE_PASSPHRASE_URL)
                attribute("inviteLocalCouncilAdminUrl", INVITE_LOCAL_COUNCIL_ADMIN_ROUTE)
                attribute("metricsUrl", METRICS_URL)
            }
        }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `systemOperatorDashboard does not include the generate passcode url when the require-passcode profile is inactive`() {
        mvc.get(SYSTEM_OPERATOR_DASHBOARD_URL).andExpect {
            status { isOk() }
            model {
                attributeDoesNotExist("generatePasscodeUrl")
            }
        }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `systemOperatorDashboard renders a link to generate a passphrase`() {
        mvc
            .get(SYSTEM_OPERATOR_DASHBOARD_URL)
            .andExpect {
                status { isOk() }
                content {
                    string(containsString("Generate a passphrase"))
                    string(containsString(GENERATE_PASSPHRASE_URL))
                }
            }
    }
}
