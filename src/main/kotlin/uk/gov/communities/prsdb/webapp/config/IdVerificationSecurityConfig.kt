package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.context.SecurityContextHolderFilter

@Configuration
@EnableMethodSecurity
class IdVerificationSecurityConfig(
    val clientRegistrationRepository: ClientRegistrationRepository,
) {
    @Bean
    @Order(1)
    fun idVerificationFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/register-as-a-landlord/verify-identity", "/id-verification/**")
            .authorizeHttpRequests { requests ->
                requests
                    .anyRequest()
                    .authenticated()
            }.oauth2Login { oauth ->
                oauth.authorizationEndpoint { authorization ->
                    authorization.configureOAuthAuthorizationToAddVerificationParameters("/id-verification/oauth2/authorize")
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

    private fun OAuth2LoginConfigurer<HttpSecurity>.AuthorizationEndpointConfig.configureOAuthAuthorizationToAddVerificationParameters(
        authorizationRequestBaseUri: String,
    ): OAuth2LoginConfigurer<HttpSecurity>.AuthorizationEndpointConfig {
        val idVerificationParameters = oneLoginIdVerificationParameters()

        return this
            .baseUri(authorizationRequestBaseUri)
            .authorizationRequestResolver(
                AdditionalParameterAddingOAuth2RequestResolver(
                    clientRegistrationRepository,
                    authorizationRequestBaseUri,
                    idVerificationParameters,
                ),
            )
    }

    private fun oneLoginIdVerificationParameters(): Map<String, String> {
        val claimsRequest =
            """{"userinfo": {
                        |"https://vocab.account.gov.uk/v1/coreIdentityJWT":null,
                        |"https://vocab.account.gov.uk/v1/returnCode":null,
                        |"https://vocab.account.gov.uk/v1/address":null}}
            """.trimMargin()
        return mapOf("vtr" to "[\"Cl.Cm.P2\"]", "claims" to claimsRequest)
    }

    private fun doesTokenContainAnyIdVerificationClaims(authenticationToken: OAuth2AuthenticationToken): Boolean {
        val user = authenticationToken.principal
        return user is OidcUser &&
            (
                user.userInfo.claims.keys
                    .any { it.contains("https://vocab.account.gov.uk") }
            )
    }
}
