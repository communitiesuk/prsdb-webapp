package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration

@Profile("!local")
@PrsdbWebConfiguration
class CloudWatchConfig {
    @Bean
    fun cloudWatchClient(): CloudWatchClient = CloudWatchClient.create()
}
