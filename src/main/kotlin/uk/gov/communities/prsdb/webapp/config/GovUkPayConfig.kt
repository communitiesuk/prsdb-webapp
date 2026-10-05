package uk.gov.communities.prsdb.webapp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestClient
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException

@PrsdbWebConfiguration
class GovUkPayConfig {
    @Value("\${gov-uk-pay.base-url}")
    lateinit var baseUrl: String

    @Value("\${gov-uk-pay.api-key}")
    lateinit var apiKey: String

    // RestClient only treats 4xx and 5xx responses as errors, so we also reject other non-2xx responses (e.g. redirects)
    @Bean("gov-uk-pay-client")
    fun govUkPayRestClient(): RestClient =
        RestClient
            .builder()
            .baseUrl(baseUrl)
            .requestInterceptor { request, body, execution ->
                request.headers.setBearerAuth(apiKey)
                execution.execute(request, body)
            }.defaultStatusHandler({ !it.is2xxSuccessful && !it.isError }) { _, response ->
                throw GovUkPayException("GOV.UK Pay responded with unexpected HTTP status ${response.statusCode.value()}")
            }.build()
}
