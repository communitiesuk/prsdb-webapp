package uk.gov.communities.prsdb.webapp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.service.notify.NotificationClient

@Configuration
class NotifyConfig {
    @Value("\${notify.api-key}")
    lateinit var apiKey: String

    @Bean
    fun notificationClient(): NotificationClient = NotificationClient(apiKey)
}
