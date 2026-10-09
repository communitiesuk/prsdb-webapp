package uk.gov.communities.prsdb.webapp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import java.net.http.HttpClient
import java.time.Duration

@PrsdbWebConfiguration
class GovUkPayConfig {
    @Value("\${gov-uk-pay.base-url}")
    lateinit var baseUrl: String

    @Value("\${gov-uk-pay.api-key}")
    lateinit var apiKey: String

    @Value("\${gov-uk-pay.connect-timeout:5s}")
    lateinit var connectTimeout: Duration

    @Value("\${gov-uk-pay.read-timeout:10s}")
    lateinit var readTimeout: Duration

    // RestClient only treats 4xx and 5xx responses as errors, so we also reject other non-2xx responses (e.g. redirects)
    @Bean("gov-uk-pay-client")
    fun govUkPayRestClient(): RestClient =
        RestClient
            .builder()
            .baseUrl(baseUrl)
            .requestFactory(createRequestFactory())
            .requestInterceptor { request, body, execution ->
                request.headers.setBearerAuth(apiKey)
                execution.execute(request, body)
            }.defaultStatusHandler({ !it.is2xxSuccessful && !it.isError }) { _, response ->
                throw GovUkPayException("GOV.UK Pay responded with unexpected HTTP status ${response.statusCode.value()}")
            }.build()

    private fun createRequestFactory() =
        JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(connectTimeout).build()).apply {
            setReadTimeout(readTimeout)
        }
}
