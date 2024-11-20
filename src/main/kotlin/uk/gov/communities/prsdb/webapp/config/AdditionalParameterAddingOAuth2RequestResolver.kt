package uk.gov.communities.prsdb.webapp.config

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest

class AdditionalParameterAddingOAuth2RequestResolver(
    clientRegistrationRepository: ClientRegistrationRepository,
    authorizationRequestBaseUri: String,
    private val additionalParameters: Map<String, Any>,
) : OAuth2AuthorizationRequestResolver {
    private val innerResolver =
        DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, authorizationRequestBaseUri)

    override fun resolve(request: HttpServletRequest?): OAuth2AuthorizationRequest? {
        val authRequest = innerResolver.resolve(request) ?: return null
        return authRequestWithAdditionalParameters(authRequest)
    }

    override fun resolve(
        request: HttpServletRequest?,
        clientRegistrationId: String?,
    ): OAuth2AuthorizationRequest? {
        val authRequest = innerResolver.resolve(request, clientRegistrationId) ?: return null
        return authRequestWithAdditionalParameters(authRequest)
    }

    private fun authRequestWithAdditionalParameters(authRequest: OAuth2AuthorizationRequest): OAuth2AuthorizationRequest? =
        OAuth2AuthorizationRequest.from(authRequest).additionalParameters(additionalParameters).build()
}
