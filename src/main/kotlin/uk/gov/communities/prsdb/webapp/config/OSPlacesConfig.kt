package uk.gov.communities.prsdb.webapp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.communities.prsdb.webapp.clients.OSPlacesClient

@Configuration
class OSPlacesConfig {
    @Value("\${os-places.api-key}")
    lateinit var apiKey: String

    @Bean
    fun osPlacesClient(): OSPlacesClient = OSPlacesClient(apiKey)
}
