package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.web.context.WebApplicationContext

abstract class LettingAgentAccessControllerTest(
    context: WebApplicationContext,
) : ControllerTest(context) {
    @BeforeEach
    fun allowPastLettingAgentAccessInterceptor() {
        whenever(lettingAgentAccessService.getTokenIsValid(any())).thenReturn(true)
        whenever(lettingAgentAccessService.isTokenAuthorisedInSession(any())).thenReturn(true)
    }
}
