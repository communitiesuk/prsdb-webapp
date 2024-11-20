package uk.gov.communities.prsdb.webapp.config

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest

class AdditionalParameterAddingRequestResolver(
    clientRegistrationRepository: ClientRegistrationRepository,
    baseUri: String,
) : OAuth2AuthorizationRequestResolver {
    private val innerResolver =
        DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, baseUri)

    override fun resolve(request: HttpServletRequest?): OAuth2AuthorizationRequest? {
        val authRequest = innerResolver.resolve(request) ?: return null
        return resolveAuthRequest(authRequest)
    }

    override fun resolve(
        request: HttpServletRequest?,
        clientRegistrationId: String?,
    ): OAuth2AuthorizationRequest? {
        val authRequest = innerResolver.resolve(request, clientRegistrationId) ?: return null
        return resolveAuthRequest(authRequest)
    }

    private fun resolveAuthRequest(authRequest: OAuth2AuthorizationRequest): OAuth2AuthorizationRequest? {
        val claimsRequest =
            """{"userinfo": {
                |"https://vocab.account.gov.uk/v1/coreIdentityJWT":null, 
                |"https://vocab.account.gov.uk/v1/returnCode":null, 
                |"https://vocab.account.gov.uk/v1/address":null}}
            """.trimMargin()
        val additionalParams =
            LinkedHashMap(authRequest.additionalParameters).apply {
                put("vtr", "[\"Cl.Cm.P2\"]")
                put("claims", claimsRequest)
            }
        return OAuth2AuthorizationRequest.from(authRequest).additionalParameters(additionalParams).build()
    }
}
