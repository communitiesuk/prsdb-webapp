package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.controllers.GeneratePasscodeController.Companion.GENERATE_PASSCODE_URL
import uk.gov.communities.prsdb.webapp.controllers.SystemOperatorDashboardController.Companion.SYSTEM_OPERATOR_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.services.PasscodeService
import kotlin.test.Test

@WebMvcTest(SystemOperatorDashboardController::class)
@ActiveProfiles("require-passcode")
class SystemOperatorDashboardControllerRequirePasscodeTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var passcodeService: PasscodeService

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `systemOperatorDashboard includes the generate passcode url when the require-passcode profile is active`() {
        mvc.get(SYSTEM_OPERATOR_DASHBOARD_URL).andExpect {
            status { isOk() }
            view { name("systemOperatorDashboard") }
            model {
                attribute("generatePasscodeUrl", GENERATE_PASSCODE_URL)
            }
        }
    }
}
