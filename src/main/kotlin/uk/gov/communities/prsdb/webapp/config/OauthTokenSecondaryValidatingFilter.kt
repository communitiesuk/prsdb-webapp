package uk.gov.communities.prsdb.webapp.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken

class OauthTokenSecondaryValidatingFilter(
    val isOauthTokenAcceptable: (OAuth2AuthenticationToken) -> Boolean,
) : Filter {
    override fun doFilter(
        request: ServletRequest?,
        response: ServletResponse?,
        chain: FilterChain?,
    ) {
        val securityContext = SecurityContextHolder.getContext()
        val auth = securityContext.authentication
        if (auth != null) {
            if (!(auth is OAuth2AuthenticationToken && isOauthTokenAcceptable(auth))) {
                SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext())
            }
        }

        chain!!.doFilter(request, response)
    }
}
