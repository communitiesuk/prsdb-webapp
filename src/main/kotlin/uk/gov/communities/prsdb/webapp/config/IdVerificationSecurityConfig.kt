package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.context.SecurityContextHolderFilter
import uk.gov.communities.prsdb.webapp.config.filters.OauthTokenSecondaryValidatingFilter
import uk.gov.communities.prsdb.webapp.config.resolvers.AdditionalParameterAddingOAuth2RequestResolver
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController

@Profile("!local | local-auth")
@Configuration
@EnableMethodSecurity
class IdVerificationSecurityConfig(
    val clientRegistrationRepository: ClientRegistrationRepository,
) {
    @Bean
    @Order(1)
    fun idVerificationFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher(
                "/$REGISTER_LANDLORD_JOURNEY_URL/${RegisterLandlordController.IDENTITY_VERIFICATION_PATH_SEGMENT}",
                "/id-verification/**",
            ).authorizeHttpRequests { requests ->
                requests
                    .anyRequest()
                    .authenticated()
            }.oauth2Login { oauth ->
                oauth.authorizationEndpoint { authorization ->
                    authorization.addIdVerificationParametersToAuthorizationWithBaseUri("/id-verification/oauth2/authorize")
                }
            }.csrf { }
            .addFilterAfter(
                OauthTokenSecondaryValidatingFilter(
                    ::doesTokenContainAnyIdVerificationClaims,
                ),
                SecurityContextHolderFilter::class.java,
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
                        |"https://vocab.account.gov.uk/v1/coreIdentityJWT":null,
                        |"https://vocab.account.gov.uk/v1/returnCode":null,
                        |"https://vocab.account.gov.uk/v1/address":null}}
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
                    .any { it.contains("https://vocab.account.gov.uk") }
            )
    }
}