package uk.gov.communities.prsdb.webapp.config.security

import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.constants.ASSETS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MAINTENANCE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.CookiesController.Companion.COOKIES_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.HealthCheckController.Companion.HEALTHCHECK_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.services.UserRolesService

@PrsdbWebConfiguration
@EnableMethodSecurity
class DefaultSecurityConfig(
    val clientRegistrationRepository: ClientRegistrationRepository,
) {
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/")
                    .permitAll()
                    .requestMatchers(HEALTHCHECK_ROUTE)
                    .permitAll()
                    .requestMatchers(RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE)
                    .permitAll()
                    .requestMatchers("/signout")
                    .permitAll()
                    .requestMatchers("/$ASSETS_PATH_SEGMENT/**")
                    .permitAll()
                    .requestMatchers("/error/**")
                    .permitAll()
                    .requestMatchers("/check/**")
                    .permitAll()
                    .requestMatchers("/local/**")
                    .permitAll()
                    .requestMatchers("$COOKIES_ROUTE/**")
                    .permitAll()
                    .requestMatchers("/$MAINTENANCE_PATH_SEGMENT")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }.oauth2Login(Customizer.withDefaults())
            .logout { logout ->
                logout.logoutSuccessHandler(oidcLogoutSuccessHandler())
            }.csrf { requests ->
                requests.ignoringRequestMatchers("/local/**")
            }

        return http.build()
    }

    @Bean
    fun allRolesOidcUserService(userRolesService: UserRolesService): OAuth2UserService<OidcUserRequest, OidcUser> =
        UserServiceFactory.create(userRolesService::getAllRolesForSubjectId)

    @Bean
    fun securityContextRepository(): SecurityContextRepository = HttpSessionSecurityContextRepository()

    private fun oidcLogoutSuccessHandler(): LogoutSuccessHandler {
        val oidcLogoutSuccessHandler = OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository)
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/signout")
        oidcLogoutSuccessHandler.setDefaultTargetUrl("/signout")
        return oidcLogoutSuccessHandler
    }
}
