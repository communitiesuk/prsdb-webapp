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
                    // Letting agent routes are only available to anonymous users (as login is not implemented for letting agents)
                    // Restricting which letting agent routes are available is handled by the LettingAgentAccessInterceptor and its config.
                    .anyRequest()
                    .anonymous()
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
