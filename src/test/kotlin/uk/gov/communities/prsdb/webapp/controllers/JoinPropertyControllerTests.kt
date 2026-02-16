package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_START_PAGE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL

@WebMvcTest(JoinPropertyController::class)
class JoinPropertyControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Test
    fun `getStartPage returns a redirect for unauthenticated user`() {
        mvc
            .get(JOIN_PROPERTY_START_PAGE_ROUTE)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `getStartPage returns 403 for unauthorized user`() {
        mvc
            .get(JOIN_PROPERTY_START_PAGE_ROUTE)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getStartPage returns 200 for authorised landlord user`() {
        mvc
            .get(JOIN_PROPERTY_START_PAGE_ROUTE)
            .andExpect {
                status { isOk() }
                model {
                    attribute("backUrl", LANDLORD_DASHBOARD_URL)
                    attribute("continueUrl", "#")
                }
            }
    }
}
