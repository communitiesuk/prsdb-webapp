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
import org.springframework.security.web.header.HeaderWriterFilter
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.config.filters.CSPNonceFilter
import uk.gov.communities.prsdb.webapp.constants.ASSETS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.ERROR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.GOOGLE_TAG_MANAGER_URL
import uk.gov.communities.prsdb.webapp.constants.GOOGLE_URL
import uk.gov.communities.prsdb.webapp.constants.MAINTENANCE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PLAUSIBLE_URL
import uk.gov.communities.prsdb.webapp.constants.REGION_1_GOOGLE_ANALYTICS_URL
import uk.gov.communities.prsdb.webapp.constants.SIGN_OUT_PATH_SEGMENT
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
                    .requestMatchers("/$SIGN_OUT_PATH_SEGMENT")
                    .permitAll()
                    .requestMatchers("/$ASSETS_PATH_SEGMENT/**")
                    .permitAll()
                    .requestMatchers("/$ERROR_PATH_SEGMENT/**")
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
            }.headers { headers ->
                headers
                    .contentSecurityPolicy { csp ->
                        csp
                            .policyDirectives(CONTENT_SECURITY_POLICY_DIRECTIVES)
                    }
                    .permissionsPolicyHeader {
                            permissions ->
                        permissions
                            .policy(PERMISSIONS_POLICY_DIRECTIVES)
                    }
            }.addFilterBefore(CSPNonceFilter(), HeaderWriterFilter::class.java)

        return http.build()
    }

    @Bean
    fun allRolesOidcUserService(userRolesService: UserRolesService): OAuth2UserService<OidcUserRequest, OidcUser> =
        UserServiceFactory.create(userRolesService::getAllRolesForSubjectId)

    @Bean
    fun securityContextRepository(): SecurityContextRepository = HttpSessionSecurityContextRepository()

    private fun oidcLogoutSuccessHandler(): LogoutSuccessHandler {
        val oidcLogoutSuccessHandler = OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository)
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/$SIGN_OUT_PATH_SEGMENT")
        oidcLogoutSuccessHandler.setDefaultTargetUrl("/$SIGN_OUT_PATH_SEGMENT")
        return oidcLogoutSuccessHandler
    }

    companion object {
        const val CONTENT_SECURITY_POLICY_DIRECTIVES =
            "default-src 'self'; " +
                "script-src 'self' 'nonce-' $PLAUSIBLE_URL $GOOGLE_TAG_MANAGER_URL; " +
                "connect-src 'self' $REGION_1_GOOGLE_ANALYTICS_URL $GOOGLE_TAG_MANAGER_URL $GOOGLE_URL $PLAUSIBLE_URL; " +
                "img-src 'self' $GOOGLE_TAG_MANAGER_URL; " +
                "style-src 'self'; " +
                "object-src 'none'; base-uri 'none'; frame-ancestors 'none';"

        // The permission policy directives are from:
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Permissions-Policy#directives
        // This is the list of permissions that we are blocking.
        const val PERMISSIONS_POLICY_DIRECTIVES =
            "accelerometer=(), aria-notify=(), attribution-reporting=(), " +
                "autoplay=(), bluetooth=(), browsing-topics=(), camera=(), captured-surface-control=(), " +
                "compute-pressure=(), cross-origin-isolated=(), deferred-fetch=(), deferred-fetch-minimal=(), " +
                "display-capture=(), encrypted-media=(), fullscreen=(), gamepad=(), geolocation=(), " +
                "gyroscope=(), hid=(), identity-credentials-get=(), idle-detection=(), language-detector=(), local-fonts=(), " +
                "magnetometer=(), microphone=(), midi=(), on-device-speech-recognition=(), otp-credentials=(), payment=(), " +
                "picture-in-picture=(), publickey-credentials-create=(), publickey-credentials-get=(), screen-wake-lock=(), " +
                "serial=(), storage-access=(), summarizer=(), translator=(), usb=(), web-share=(), " +
                "window-management=(), xr-spatial-tracking=()"
    }
}
