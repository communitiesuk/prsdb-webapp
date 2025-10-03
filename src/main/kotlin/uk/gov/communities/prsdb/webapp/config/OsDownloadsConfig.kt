package uk.gov.communities.prsdb.webapp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.communities.prsdb.webapp.clients.OsDownloadsClient
import java.net.http.HttpClient

// TODO PRSD-1021: Change annotation to PrsdbProcessConfiguration when ExampleOsDownloadsController is deleted
@Configuration
class OsDownloadsConfig {
    @Value("\${os.downloads.base-url}")
    lateinit var baseURL: String

    @Value("\${os.api-key}")
    lateinit var apiKey: String

    val httpClient: HttpClient = HttpClient.newHttpClient()

    @Bean
    fun osDownloadsClient() = OsDownloadsClient(httpClient, baseURL, apiKey)
}
