package uk.gov.communities.prsdb.webapp.config

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest

class AdditionalParameterAddingRequestResolver(
    repo: ClientRegistrationRepository,
    private val additionalKey: String,
    private val additionalValue: Any,
) : OAuth2AuthorizationRequestResolver {
    private val innerResolver = DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization")

    override fun resolve(request: HttpServletRequest?): OAuth2AuthorizationRequest? {
        val authRequest = innerResolver.resolve(request) ?: return null
        val additionalParams = LinkedHashMap(authRequest.additionalParameters).apply { put(additionalKey, additionalValue) }
        return OAuth2AuthorizationRequest.from(authRequest).additionalParameters(additionalParams).build()
    }

    override fun resolve(
        request: HttpServletRequest?,
        clientRegistrationId: String?,
    ): OAuth2AuthorizationRequest? {
        val authRequest = innerResolver.resolve(request, clientRegistrationId) ?: return null
        val additionalParams = LinkedHashMap(authRequest.additionalParameters).apply { put("vtr", "Cl.Cm.P2") }
        return OAuth2AuthorizationRequest.from(authRequest).additionalParameters(additionalParams).build()
    }
}
