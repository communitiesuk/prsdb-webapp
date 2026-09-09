package uk.gov.communities.prsdb.webapp.config

import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.config.interceptors.LettingAgentAccessInterceptor
import uk.gov.communities.prsdb.webapp.config.security.LettingAgentSecurityConfig.Companion.LETTING_AGENT_ROUTES_PATTERN
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
            // Exclude the whole invitation (set/enter password) journey and its invalid-link page so an
            // unauthorised agent can still reach the password journey without a redirect loop.
            .excludePathPatterns(
                LETTING_AGENT_INVITATION_ROUTE,
                "$LETTING_AGENT_INVITATION_ROUTE/**",
            )
    }
}
