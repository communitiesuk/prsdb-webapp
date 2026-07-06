package uk.gov.communities.prsdb.webapp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestClient
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration

@PrsdbWebConfiguration
class PlausibleApiConfig {
    @Value("\${plausible.base-url}")
    lateinit var baseUrl: String

    @Value("\${plausible.api-key}")
    lateinit var apiKey: String

    @Bean("plausible-stats-client")
    fun plausibleStatsClient(): RestClient =
        RestClient
            .builder()
            .baseUrl(baseUrl)
            .requestInterceptor { request, body, execution ->
                request.headers.setBearerAuth(apiKey)
                execution.execute(request, body)
            }.build()
}
