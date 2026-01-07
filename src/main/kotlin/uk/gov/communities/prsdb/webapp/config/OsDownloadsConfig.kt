package uk.gov.communities.prsdb.webapp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import uk.gov.communities.prsdb.webapp.annotations.processAnnotations.PrsdbProcessConfiguration
import uk.gov.communities.prsdb.webapp.clients.OsDownloadsClient
import java.net.http.HttpClient

@PrsdbProcessConfiguration
class OsDownloadsConfig {
    @Value("\${os.downloads.base-url}")
    lateinit var baseURL: String

    @Value("\${os.api-key}")
    lateinit var apiKey: String

    val httpClient: HttpClient = HttpClient.newHttpClient()

    @Bean
    fun osDownloadsClient() = OsDownloadsClient(httpClient, baseURL, apiKey)
}
