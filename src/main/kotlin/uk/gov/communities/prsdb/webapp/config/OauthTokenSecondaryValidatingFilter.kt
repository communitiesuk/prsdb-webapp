package uk.gov.communities.prsdb.webapp.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.context.SecurityContextHolderFilter
import org.springframework.security.web.context.SecurityContextRepository

/**
 * This is a [SecurityFilterChain] [Filter] which conditionally clears the SecurityContextHolder depending on the OAuth2AuthenticationToken stored.
 * The [SecurityContextHolder] stores the [SecurityContext] for the scope of a specific request.
 * The [SecurityContextRepository] is a persistent store of the [SecurityContext] and allows access to the [SecurityContext] for multiple requests.
 * The [SecurityContextHolderFilter] takes the [SecurityContext] saved in the [SecurityContextRepository] and puts it into the [SecurityContextHolder] so it can be accessed within that request.
 * This filter should be added to the [SecurityFilterChain] directly after the [SecurityContextHolderFilter], where it will validate the [SecurityContext] that has just been extracted from the [SecurityContextRepository] and set the [SecurityContext] in the [SecurityContextHolder] to be empty if it is not valid.
 * Because it does not save any changes to the [SecurityContextRepository], this does not affect the persistent authentication state and only requests to endpoints with this filter will treat that authentication as invalid.
 */
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
