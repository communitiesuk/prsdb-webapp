package uk.gov.communities.prsdb.webapp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.clients.OSPlacesClient
import java.net.http.HttpClient

@PrsdbWebConfiguration
class OSPlacesConfig {
    @Value("\${os-places.base-url}")
    lateinit var baseURL: String

    @Value("\${os-places.api-key}")
    lateinit var apiKey: String

    val httpClient: HttpClient = HttpClient.newHttpClient()

    @Bean
    fun osPlacesClient(): OSPlacesClient = OSPlacesClient(httpClient, baseURL, apiKey)
}
