package uk.gov.communities.prsdb.webapp.config

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory
import io.awspring.cloud.sqs.listener.QueueNotFoundStrategy
import io.awspring.cloud.sqs.listener.errorhandler.ExponentialBackoffErrorHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.services.sqs.SqsAsyncClient

@Configuration
@Profile(EmailQueueListenerConfig.EMAIL_QUEUE_LISTENER_PROFILES)
class EmailQueueListenerConfig {
    @Bean(EMAIL_QUEUE_LISTENER_CONTAINER_FACTORY)
    fun emailQueueListenerContainerFactory(
        sqsAsyncClient: SqsAsyncClient,
        @Value("\${email-queue.listener.auto-startup}") autoStartup: Boolean,
        @Value("\${email-queue.listener.max-concurrent-messages}") maxConcurrentMessages: Int,
        @Value("\${email-queue.listener.initial-visibility-timeout-seconds}") initialVisibilityTimeoutSeconds: Int,
        @Value("\${email-queue.listener.visibility-timeout-multiplier}") visibilityTimeoutMultiplier: Double,
        @Value("\${email-queue.listener.max-visibility-timeout-seconds}") maxVisibilityTimeoutSeconds: Int,
    ): SqsMessageListenerContainerFactory<Any> =
        SqsMessageListenerContainerFactory
            .builder<Any>()
            .sqsAsyncClient(sqsAsyncClient)
            .configure { options ->
                options
                    .autoStartup(autoStartup)
                    .maxConcurrentMessages(maxConcurrentMessages)
                    .maxMessagesPerPoll(maxConcurrentMessages)
                    .queueNotFoundStrategy(QueueNotFoundStrategy.FAIL)
            }.errorHandler(
                ExponentialBackoffErrorHandler
                    .builder<Any>()
                    .initialVisibilityTimeoutSeconds(initialVisibilityTimeoutSeconds)
                    .multiplier(visibilityTimeoutMultiplier)
                    .maxVisibilityTimeoutSeconds(maxVisibilityTimeoutSeconds)
                    .build(),
            ).build()

    companion object {
        const val EMAIL_QUEUE_LISTENER_PROFILES = "email-worker | (local & !web-server-deactivated)"
        const val EMAIL_QUEUE_LISTENER_CONTAINER_FACTORY = "emailQueueListenerContainerFactory"
    }
}
