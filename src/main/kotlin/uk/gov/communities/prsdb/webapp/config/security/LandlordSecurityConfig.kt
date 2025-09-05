package uk.gov.communities.prsdb.webapp.config.security

import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.context.SecurityContextHolderFilter
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.csrf.CsrfFilter
import org.springframework.security.web.csrf.CsrfTokenRepository
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.config.filters.InvalidCoreIdentityFilter
import uk.gov.communities.prsdb.webapp.config.filters.MultipartFormDataFilter
import uk.gov.communities.prsdb.webapp.config.filters.OauthTokenSecondaryValidatingFilter
import uk.gov.communities.prsdb.webapp.config.resolvers.AdditionalParameterAddingOAuth2RequestResolver
import uk.gov.communities.prsdb.webapp.constants.OneLoginClaimKeys
import uk.gov.communities.prsdb.webapp.controllers.BetaFeedbackController
import uk.gov.communities.prsdb.webapp.controllers.LandlordPrivacyNoticeController
import uk.gov.communities.prsdb.webapp.controllers.PasscodeEntryController
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.services.UserRolesService

@PrsdbWebConfiguration
@EnableMethodSecurity
class LandlordSecurityConfig(
    val clientRegistrationRepository: ClientRegistrationRepository,
    val securityContextRepository: SecurityContextRepository,
    val userRolesService: UserRolesService,
) {
    @Bean
    @Order(2)
    fun landlordSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/landlord/**")
            // Required to allow csrf token to be stored in the session on public pages
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.ALWAYS) }
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers(RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE)
                    .permitAll()
                    .requestMatchers(PasscodeEntryController.PASSCODE_ENTRY_ROUTE)
                    .permitAll()
                    .requestMatchers(PasscodeEntryController.INVALID_PASSCODE_ROUTE)
                    .permitAll()
                    .requestMatchers(RegisterLandlordController.LANDLORD_REGISTRATION_START_PAGE_ROUTE)
                    .permitAll()
                    .requestMatchers(LandlordPrivacyNoticeController.LANDLORD_PRIVACY_NOTICE_ROUTE)
                    .permitAll()
                    .requestMatchers("${BetaFeedbackController.LANDLORD_FEEDBACK_URL}/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }.oauth2Login { oauth ->
                oauth.userInfoEndpoint { userInfo ->
                    userInfo.oidcUserService(landlordOidcUserService())
                }
                oauth.redirectionEndpoint { redirection ->
                    redirection.baseUri("/landlord/login/oauth2/code/one-login")
                }
            }.csrf { requests ->
                requests.ignoringRequestMatchers("/local/**").csrfTokenRepository(csrfTokenRepository())
            }.addFilterBefore(MultipartFormDataFilter(csrfTokenRepository()), CsrfFilter::class.java)

        return http.build()
    }

    @Bean
    @Order(1)
    fun idVerificationFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher(
                "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/" +
                    RegisterLandlordController.IDENTITY_VERIFICATION_PATH_SEGMENT,
                "/id-verification/**",
            ).authorizeHttpRequests { requests ->
                requests
                    .anyRequest()
                    .authenticated()
            }.oauth2Login { oauth ->
                oauth.authorizationEndpoint { authorization ->
                    authorization.addIdVerificationParametersToAuthorizationWithBaseUri("/id-verification/oauth2/authorize")
                }
                oauth.userInfoEndpoint { userInfo ->
                    userInfo.oidcUserService(landlordOidcUserService())
                }
            }.csrf { }
            .addFilterAfter(
                OauthTokenSecondaryValidatingFilter(
                    ::doesTokenContainAnyIdVerificationClaims,
                ),
                SecurityContextHolderFilter::class.java,
            ).addFilterAfter(
                InvalidCoreIdentityFilter(securityContextRepository),
                OauthTokenSecondaryValidatingFilter::class.java,
            )

        return http.build()
    }

    /**
     * Sets the authorization base uri to the specified route and adds claims requests and vector of trust parameters to authorization requests made at that route.
     *
     * @param authorizationRequestBaseUri The authorization endpoint used for this filter chain. This **must** be matched by the security matchers for the filter to
     * ensure that the parameters are added to authorization requests
     */
    private fun OAuth2LoginConfigurer<HttpSecurity>.AuthorizationEndpointConfig.addIdVerificationParametersToAuthorizationWithBaseUri(
        authorizationRequestBaseUri: String,
    ): OAuth2LoginConfigurer<HttpSecurity>.AuthorizationEndpointConfig =
        this
            .baseUri(authorizationRequestBaseUri)
            .authorizationRequestResolver(
                AdditionalParameterAddingOAuth2RequestResolver(
                    clientRegistrationRepository,
                    authorizationRequestBaseUri,
                    oneLoginIdVerificationParameters(),
                ),
            )

    /**
     * Additional parameters that must be added to authorization requests to one-login for the id verification journey.
     * [One Login Documentation Reference](https://docs.sign-in.service.gov.uk/integrate-with-integration-environment/authenticate-your-user/#make-a-request-for-authentication)
     */
    private fun oneLoginIdVerificationParameters(): Map<String, String> {
        val claimsRequest =
            """{"userinfo": {
                        |"${OneLoginClaimKeys.CORE_IDENTITY}":null,
                        |"${OneLoginClaimKeys.ADDRESS}":null,
                        |"${OneLoginClaimKeys.RETURN_CODE}":null}}
            """.trimMargin()
        return mapOf("vtr" to "[\"Cl.Cm.P2\"]", "claims" to claimsRequest)
    }

    /**
     * Returns true if the authentication token contains an OidcUser with any claim issued by One Login's Id Verification service.
     * [One Login Documentation Reference](https://docs.sign-in.service.gov.uk/integrate-with-integration-environment/prove-users-identity/)
     */
    private fun doesTokenContainAnyIdVerificationClaims(authenticationToken: OAuth2AuthenticationToken): Boolean {
        val user = authenticationToken.principal
        return user is OidcUser &&
            (
                user.userInfo.claims.keys
                    .any { it.contains(OneLoginClaimKeys.DOMAIN) }
            )
    }

    fun landlordOidcUserService(): OAuth2UserService<OidcUserRequest, OidcUser> =
        UserServiceFactory.create(userRolesService::getLandlordRolesForSubjectId)

    @Bean
    fun csrfTokenRepository(): CsrfTokenRepository = HttpSessionCsrfTokenRepository()
}
