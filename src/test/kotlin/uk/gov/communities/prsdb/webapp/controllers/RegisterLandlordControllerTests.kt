package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.OneLoginIdentityService

@WebMvcTest(RegisterLandlordController::class)
class RegisterLandlordControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    lateinit var landlordRegistrationJourneyFactory: LandlordRegistrationJourneyFactory

    @MockitoBean
    lateinit var identityService: OneLoginIdentityService

    @MockitoBean
    lateinit var landlordService: LandlordService

    @Test
    fun `RegisterLandlordController returns 200 for unauthenticated user`() {
        mvc.get("/register-as-a-landlord").andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `RegisterLandlordController returns 308 for authenticated user with trailing slash`() {
        mvc.get("/register-as-a-landlord/").andExpect {
            status { isPermanentRedirect() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getVerifyIdentity returns 302 for authenticated user with Landlord role`() {
        whenever(userRolesService.getHasLandlordUserRole(any())).thenReturn(true)
        mvc
            .get("/register-as-a-landlord/verify-identity") {
                with(oidcLogin())
            }.andExpectAll {
                status { is3xxRedirection() }
                redirectedUrl("/landlord/dashboard")
            }
    }
}
