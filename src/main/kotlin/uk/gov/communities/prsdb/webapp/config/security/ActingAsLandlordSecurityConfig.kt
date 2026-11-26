package uk.gov.communities.prsdb.webapp.config.security

import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.context.SecurityContextHolderFilter
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.csrf.CsrfTokenRepository
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.config.filters.ActingAsLandlordFilter
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.ROLE_ACTING_AS_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.services.ActingAsLandlordService
import uk.gov.communities.prsdb.webapp.services.UserRolesService

@PrsdbWebConfiguration
@EnableMethodSecurity
class ActingAsLandlordSecurityConfig(
    private val actingAsLandlordService: ActingAsLandlordService,
    private val userRolesService: UserRolesService,
    private val csrfTokenRepository: CsrfTokenRepository,
    private val securityContextRepository: SecurityContextRepository,
) {
    @Bean
    @Order(5)
    fun actingAsLandlordSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher(ACTING_AS_LANDLORD_ROUTES_PATTERN)
            .applyLandlordChainSettings(csrfTokenRepository)
            // The chain only admits signed-in system operators, who already have a session, so unlike the landlord chain it
            // doesn't need sessionCreationPolicy(ALWAYS), which would add a SessionManagementFilter that saves the acting context
            .sessionManagement { it.requireExplicitAuthenticationStrategy(true) }
            // Chains don't use the SecurityContextRepository bean by default, and the bean refuses to save acting tokens
            .securityContext { it.securityContextRepository(securityContextRepository) }
            .authorizeHttpRequests { requests ->
                requests
                    .anyRequest()
                    .hasAuthority(ROLE_ACTING_AS_LANDLORD)
            }.exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint(LoginUrlAuthenticationEntryPoint("/oauth2/authorization/one-login"))
            }.addFilterAfter(
                ActingAsLandlordFilter(actingAsLandlordService, userRolesService),
                SecurityContextHolderFilter::class.java,
            )

        return http.build()
    }

    companion object {
        const val ACTING_AS_LANDLORD_ROUTES_PATTERN = "/$SYSTEM_OPERATOR_PATH_SEGMENT/$LANDLORD_PATH_SEGMENT/**"
    }
}
