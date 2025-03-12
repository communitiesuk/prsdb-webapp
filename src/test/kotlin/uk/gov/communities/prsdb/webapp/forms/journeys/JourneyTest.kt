package uk.gov.communities.prsdb.webapp.forms.journeys

import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

abstract class JourneyTest {
    protected fun setMockUser(username: String) {
        val authentication = mock<Authentication>()
        whenever(authentication.name).thenReturn(username)
        val context = mock<SecurityContext>()
        whenever(context.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(context)
    }
}
