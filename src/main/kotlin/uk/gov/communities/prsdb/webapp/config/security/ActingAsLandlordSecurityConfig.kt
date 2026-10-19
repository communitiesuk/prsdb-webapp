package uk.gov.communities.prsdb.webapp.config.security

import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.context.SecurityContextHolderFilter
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.csrf.CsrfFilter
import org.springframework.security.web.csrf.CsrfTokenRepository
import org.springframework.security.web.header.HeaderWriterFilter
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.config.filters.ActingAsLandlordFilter
import uk.gov.communities.prsdb.webapp.config.filters.CSPNonceFilter
import uk.gov.communities.prsdb.webapp.config.filters.MultipartFormDataFilter
import uk.gov.communities.prsdb.webapp.config.security.DefaultSecurityConfig.Companion.CONTENT_SECURITY_POLICY_DIRECTIVES
import uk.gov.communities.prsdb.webapp.config.security.DefaultSecurityConfig.Companion.PERMISSIONS_POLICY_DIRECTIVES
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.ROLE_ACTING_AS_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.services.UserRolesService

@PrsdbWebConfiguration
@EnableMethodSecurity
class ActingAsLandlordSecurityConfig(
    private val userRolesService: UserRolesService,
    private val csrfTokenRepository: CsrfTokenRepository,
    private val securityContextRepository: SecurityContextRepository,
) {
    @Bean
    @Order(5)
    fun actingAsLandlordSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher(ACTING_AS_LANDLORD_ROUTES_PATTERN)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.ALWAYS) }
            .securityContext { it.securityContextRepository(securityContextRepository) }
            .authorizeHttpRequests { requests ->
                requests
                    .anyRequest()
                    .hasAuthority(ROLE_ACTING_AS_LANDLORD)
            }.exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint(LoginUrlAuthenticationEntryPoint("/oauth2/authorization/one-login"))
            }.csrf { csrf ->
                csrf.csrfTokenRepository(csrfTokenRepository)
            }.addFilterAfter(ActingAsLandlordFilter(userRolesService), SecurityContextHolderFilter::class.java)
            .addFilterBefore(MultipartFormDataFilter(csrfTokenRepository), CsrfFilter::class.java)
            .headers { headers ->
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

    companion object {
        const val ACTING_AS_LANDLORD_ROUTES_PATTERN = "/$SYSTEM_OPERATOR_PATH_SEGMENT/$LANDLORD_PATH_SEGMENT/**"
    }
}
