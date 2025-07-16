package uk.gov.communities.prsdb.webapp.config.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser

class UserServiceFactory {
    companion object {
        fun create(roleIssuer: (String) -> List<String>): OAuth2UserService<OidcUserRequest, OidcUser> {
            val delegate = OidcUserService()
            return OAuth2UserService { userRequest ->
                val oidcUser = delegate.loadUser(userRequest)
                val subjectId = oidcUser.subject
                val mappedAuthorities = HashSet<GrantedAuthority>()
                if (subjectId != null) {
                    val userRoles = roleIssuer(subjectId)
                    mappedAuthorities.addAll(
                        userRoles.map { role ->
                            SimpleGrantedAuthority(role)
                        },
                    )
                }
                DefaultOidcUser(mappedAuthorities, oidcUser.idToken, oidcUser.userInfo)
            }
        }
    }
}
