package uk.gov.communities.prsdb.webapp.config.security

import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.services.UserRolesService

@PrsdbWebConfiguration
@EnableMethodSecurity
class LocalAuthoritySecurityConfig(
    val userRolesService: UserRolesService,
) {
    @Bean
    @Order(3)
    fun localAuthoritySecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/local-authority/**")
            .authorizeHttpRequests { requests ->
                requests
                    .anyRequest()
                    .authenticated()
            }.oauth2Login { oauth ->
                oauth.userInfoEndpoint { userInfo ->
                    userInfo.oidcUserService(localAuthorityOidcUserService())
                }
                oauth.redirectionEndpoint { redirection ->
                    redirection.baseUri("/local-authority/login/oauth2/code/one-login")
                }
            }.csrf { }

        return http.build()
    }

    fun localAuthorityOidcUserService(): OAuth2UserService<OidcUserRequest, OidcUser> =
        UserServiceFactory.create(userRolesService::getLocalAuthorityRolesForSubjectId)
}
