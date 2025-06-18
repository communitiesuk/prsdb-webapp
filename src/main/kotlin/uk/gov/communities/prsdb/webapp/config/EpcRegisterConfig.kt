package uk.gov.communities.prsdb.webapp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.RestClient
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebConfiguration

@PrsdbWebConfiguration
class EpcRegisterConfig(
    private val authorizedClientManager: OAuth2AuthorizedClientManager,
) {
    @Value("\${epc.base-url}")
    lateinit var baseUrl: String

    @Bean("epc-client")
    fun epcWebClient(): RestClient =
        RestClient
            .builder()
            .requestInterceptor { request, body, execution ->
                val authRequest =
                    OAuth2AuthorizeRequest
                        .withClientRegistrationId("epc-register")
                        .principal("epc-register")
                        .build()

                val authorizedClient = authorizedClientManager.authorize(authRequest)
                val token = authorizedClient?.accessToken?.tokenValue ?: ""

                request.headers.setBearerAuth(token)
                execution.execute(request, body)
            }.baseUrl(baseUrl)
            .build()
}
