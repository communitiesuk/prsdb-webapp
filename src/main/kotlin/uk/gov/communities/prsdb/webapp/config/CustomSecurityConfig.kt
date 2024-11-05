package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import uk.gov.communities.prsdb.webapp.services.UserRolesService

@Configuration
class CustomSecurityConfig(
    val clientRegistrationRepository: ClientRegistrationRepository,
) {
    @Bean
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
            }.oauth2Login(Customizer.withDefaults())
            .logout { logout ->
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

@Configuration
@EnableMethodSecurity
class EnableMethodSecurityConfig
