package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.OneLoginClaimKeys
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.OneLoginIdentityService
import java.time.Instant

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
        mvc.get(RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE).andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `RegisterLandlordController returns 308 for authenticated user with trailing slash`() {
        mvc.get("${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/").andExpect {
            status { isPermanentRedirect() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getPrivacyNotice returns 302 for authenticated user with Landlord role`() {
        val idToken = OidcIdToken("token", Instant.now(), Instant.now().plusSeconds(60), mapOf("sub" to "123"))
        val userInfo = OidcUserInfo(mapOf(OneLoginClaimKeys.DOMAIN to "123"))
        val oidcUser: OidcUser = DefaultOidcUser(listOf(), idToken, userInfo)

        whenever(userRolesService.getHasLandlordUserRole(any())).thenReturn(true)
        mvc
            .get(RegisterLandlordController.LANDLORD_REGISTRATION_PRIVACY_NOTICE_ROUTE) {
                with(oidcLogin().oidcUser(oidcUser))
            }.andExpectAll {
                status { is3xxRedirection() }
                redirectedUrl("/landlord/dashboard")
            }
    }
}
