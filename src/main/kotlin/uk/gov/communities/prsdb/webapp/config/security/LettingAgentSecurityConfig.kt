package uk.gov.communities.prsdb.webapp.config.security

import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.header.HeaderWriterFilter
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.config.filters.CSPNonceFilter
import uk.gov.communities.prsdb.webapp.config.security.DefaultSecurityConfig.Companion.CONTENT_SECURITY_POLICY_DIRECTIVES
import uk.gov.communities.prsdb.webapp.config.security.DefaultSecurityConfig.Companion.PERMISSIONS_POLICY_DIRECTIVES
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENT_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentInvitationController.Companion.LETTING_AGENT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentPropertyDetailsController.Companion.LETTING_AGENT_PROPERTY_DETAILS_ROUTE

@PrsdbWebConfiguration
@EnableMethodSecurity
class LettingAgentSecurityConfig {
    @Bean
    @Order(2)
    fun lettingAgentSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/$LANDLORD_PATH_SEGMENT/$LETTING_AGENT_PATH_SEGMENT/**")
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.ALWAYS) }
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers(
                        // The whole letting-agent invitation journey (start, set/enter password,
                        // confirmation, invalid-link, and any future step) is pre-authentication: the
                        // letting agent is not a logged-in One Login user, so these are anonymous-only.
                        LETTING_AGENT_INVITATION_ROUTE,
                        "$LETTING_AGENT_INVITATION_ROUTE/**",
                    ).anonymous()
                    .requestMatchers(
                        // PDJB-1659: Left anonymous deliberately. Access to letting-agent property pages is
                        // a session permission (a validated invitation token held in the session) enforced
                        // by LettingAgentAccessInterceptor, not a Spring role. No ROLE_LETTING_AGENT is granted.
                        // The /** matcher covers property-details sub-routes (e.g. update journeys), which the
                        // interceptor also guards; without it Spring Security would block anonymous agents there.
                        LETTING_AGENT_PROPERTY_DETAILS_ROUTE,
                        "$LETTING_AGENT_PROPERTY_DETAILS_ROUTE/**",
                    ).anonymous()
                    .anyRequest()
                    .authenticated()
            }.headers { headers ->
                headers
                    .contentSecurityPolicy { csp ->
                        csp
                            .policyDirectives(CONTENT_SECURITY_POLICY_DIRECTIVES)
                    }.permissionsPolicyHeader { permissions ->
                        permissions
                            .policy(PERMISSIONS_POLICY_DIRECTIVES)
                    }
            }.addFilterBefore(CSPNonceFilter(), HeaderWriterFilter::class.java)

        return http.build()
    }
}
