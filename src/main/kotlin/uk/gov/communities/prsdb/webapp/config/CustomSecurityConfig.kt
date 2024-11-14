package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.security.web.context.SecurityContextHolderFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import uk.gov.communities.prsdb.webapp.services.UserRolesService

@Configuration
@EnableMethodSecurity
class CustomSecurityConfig(
    val clientRegistrationRepository: ClientRegistrationRepository,
) {
    @Bean
    @Order(1)
    fun verificationFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatchers { config ->
                config.requestMatchers(
                    AntPathRequestMatcher("/local-authority/**"),
                )
            }.authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/local/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }.oauth2Login { config ->
                config.loginPage("/oauth2/authorization/one-login-id")
                config.authorizationEndpoint { customizer ->
                    customizer.authorizationRequestResolver(
                        AdditionalParameterAddingRequestResolver(clientRegistrationRepository, "vtr", "Cl.Cm.P2"),
                    )
                }
            }.logout { logout ->
                logout.logoutSuccessHandler(oidcLogoutSuccessHandler())
            }.csrf { requests ->
                requests.ignoringRequestMatchers("/local/**")
            }.addFilterAfter(OauthTokenSecondaryValidatingFilter(::isOauthTokenValid), SecurityContextHolderFilter::class.java)

        return http.build()
    }

    fun isOauthTokenValid(token: OAuth2AuthenticationToken?) = token?.principal?.attributes?.get("vot") == "Cl.Cm.P2"

    @Bean
    @Order(2)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/")
                    .permitAll()
                    .requestMatchers("/register-as-a-landlord")
                    .permitAll()
                    .requestMatchers("/signout")
                    .permitAll()
                    .requestMatchers("/assets/**")
                    .permitAll()
                    .requestMatchers("/error/**")
                    .permitAll()
                    .requestMatchers("/check/**")
                    .permitAll()
                    .requestMatchers("/local/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }.oauth2Login { config ->
                config.loginPage("/oauth2/authorization/one-login")
            }.logout { logout ->
                logout.logoutSuccessHandler(oidcLogoutSuccessHandler())
            }.csrf { requests ->
                requests.ignoringRequestMatchers("/local/**")
            }

        return http.build()
    }

    @Bean
    fun oidcUserService(userRolesService: UserRolesService): OAuth2UserService<OidcUserRequest, OidcUser> {
        val delegate = OidcUserService()

        return OAuth2UserService { userRequest ->
            val oidcUser = delegate.loadUser(userRequest)
            val subjectId = oidcUser.subject
            val mappedAuthorities = HashSet<GrantedAuthority>()
            if (subjectId != null) {
                val userRoles = userRolesService.getRolesForSubjectId(subjectId)
                mappedAuthorities.addAll(
                    userRoles.map { role ->
                        SimpleGrantedAuthority(role)
                    },
                )
            }
            DefaultOidcUser(mappedAuthorities, oidcUser.idToken, oidcUser.userInfo)
        }
    }

    private fun oidcLogoutSuccessHandler(): LogoutSuccessHandler {
        val oidcLogoutSuccessHandler = OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository)
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/signout")
        oidcLogoutSuccessHandler.setDefaultTargetUrl("/signout")
        return oidcLogoutSuccessHandler
    }
}
