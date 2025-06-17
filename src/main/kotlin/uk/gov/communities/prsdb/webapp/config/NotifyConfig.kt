package uk.gov.communities.prsdb.webapp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebConfiguration
import uk.gov.service.notify.NotificationClient

@PrsdbWebConfiguration
class NotifyConfig {
    @Value("\${notify.api-key}")
    lateinit var apiKey: String

    @Bean
    fun notificationClient(): NotificationClient = NotificationClient(apiKey)
}
