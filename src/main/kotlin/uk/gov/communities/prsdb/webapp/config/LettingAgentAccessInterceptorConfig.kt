package uk.gov.communities.prsdb.webapp.config

import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.config.interceptors.LettingAgentAccessInterceptor
import uk.gov.communities.prsdb.webapp.config.security.LettingAgentSecurityConfig.Companion.LETTING_AGENT_ROUTES_PATTERN
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentInvitationController.Companion.LETTING_AGENT_INVALID_LINK_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentInvitationController.Companion.LETTING_AGENT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService

@PrsdbWebConfiguration
class LettingAgentAccessInterceptorConfig(
    private val lettingAgentAccessService: LettingAgentAccessService,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry
            .addInterceptor(LettingAgentAccessInterceptor(lettingAgentAccessService))
            .addPathPatterns(LETTING_AGENT_ROUTES_PATTERN)
            // The bare invitation entry validates its own token query parameter and redirects, and the
            // invalid-link page must always be reachable to avoid a redirect loop. Every other invitation
            // step page is still intercepted so a token revoked mid-journey is caught on the password pages.
            .excludePathPatterns(
                LETTING_AGENT_INVITATION_ROUTE,
                LETTING_AGENT_INVALID_LINK_ROUTE,
            )
    }
}
