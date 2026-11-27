package uk.gov.communities.prsdb.webapp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ClientCredentialsOAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.OAuth2AuthorizationException
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.web.client.RestClient
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.exceptions.EncodedException
import java.net.http.HttpClient
import java.time.Duration

@PrsdbWebConfiguration
class EncodedConfig(
    private val clientRegistrationRepository: ClientRegistrationRepository,
) {
    @Value("\${encoded.base-url}")
    lateinit var baseUrl: String

    @Value("\${encoded.connect-timeout}")
    lateinit var connectTimeout: Duration

    @Value("\${encoded.read-timeout}")
    lateinit var readTimeout: Duration

    @Bean("encoded-client")
    fun encodedRestClient(): RestClient = createRestClient(createRequestFactory())

    // RestClient only treats 4xx and 5xx responses as errors, so we also reject other non-2xx responses (e.g. redirects)
    internal fun createRestClient(requestFactory: ClientHttpRequestFactory): RestClient {
        val authorizedClientManager = createAuthorizedClientManager(requestFactory)
        return RestClient
            .builder()
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .requestInterceptor { request, body, execution ->
                request.headers.setBearerAuth(getAccessToken(authorizedClientManager))
                execution.execute(request, body)
            }.defaultStatusHandler({ !it.is2xxSuccessful && !it.isError }) { _, response ->
                throw EncodedException("Encoded responded with unexpected HTTP status ${response.statusCode.value()}")
            }.build()
    }

    // Not a bean, as that would stop Spring Security creating the default manager that EpcRegisterConfig uses
    private fun createAuthorizedClientManager(requestFactory: ClientHttpRequestFactory): OAuth2AuthorizedClientManager {
        val tokenResponseClient =
            RestClientClientCredentialsTokenResponseClient().apply {
                setRestClient(
                    RestClient
                        .builder()
                        .requestFactory(requestFactory)
                        .configureMessageConverters { converters ->
                            converters.addCustomConverter(FormHttpMessageConverter())
                            converters.addCustomConverter(OAuth2AccessTokenResponseHttpMessageConverter())
                        }.defaultStatusHandler(OAuth2ErrorResponseErrorHandler())
                        .build(),
                )
            }

        return AuthorizedClientServiceOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository),
        ).apply {
            setAuthorizedClientProvider(
                ClientCredentialsOAuth2AuthorizedClientProvider().apply { setAccessTokenResponseClient(tokenResponseClient) },
            )
        }
    }

    private fun getAccessToken(authorizedClientManager: OAuth2AuthorizedClientManager): String {
        val authorizeRequest =
            OAuth2AuthorizeRequest
                .withClientRegistrationId(CLIENT_REGISTRATION_ID)
                .principal(CLIENT_REGISTRATION_ID)
                .build()
        val authorizedClient =
            try {
                authorizedClientManager.authorize(authorizeRequest)
            } catch (exception: OAuth2AuthorizationException) {
                throw EncodedException("Failed to get an Encoded access token: ${exception.error.errorCode}", exception)
            }
        return authorizedClient?.accessToken?.tokenValue
            ?: throw EncodedException("Failed to get an Encoded access token: no authorised client was returned")
    }

    private fun createRequestFactory() =
        JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(connectTimeout).build()).apply {
            setReadTimeout(readTimeout)
        }

    companion object {
        const val CLIENT_REGISTRATION_ID = "encoded"
    }
}
