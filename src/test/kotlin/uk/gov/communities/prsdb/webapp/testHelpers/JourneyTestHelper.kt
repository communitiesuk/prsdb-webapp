package uk.gov.communities.prsdb.webapp.testHelpers

import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser

class JourneyTestHelper {
    companion object {
        fun setMockUser(username: String) {
            val authentication = mock<Authentication>()
            whenever(authentication.name).thenReturn(username)
            val context = mock<SecurityContext>()
            whenever(context.authentication).thenReturn(authentication)
            SecurityContextHolder.setContext(context)
        }

        fun setMockOidcPrincipal(username: String) {
            val oidcUser = mock<OidcUser>()
            whenever(oidcUser.name).thenReturn(username)
            val authentication = mock<Authentication>()
            whenever(authentication.principal).thenReturn(oidcUser)
            val context = mock<SecurityContext>()
            whenever(context.authentication).thenReturn(authentication)
            SecurityContextHolder.setContext(context)
        }
    }
}
