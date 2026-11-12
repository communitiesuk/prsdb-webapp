package uk.gov.communities.prsdb.webapp.config

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory
import kotlinx.serialization.SerializationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import uk.gov.communities.prsdb.webapp.exceptions.InvalidScanResultMessageException
import uk.gov.communities.prsdb.webapp.exceptions.PersistentScanResultProcessingException

@Configuration
@Profile(ScanResultQueueListenerConfig.SCAN_RESULT_QUEUE_LISTENER_PROFILES)
class ScanResultQueueListenerConfig {
    @Bean(SCAN_RESULT_QUEUE_LISTENER_CONTAINER_FACTORY)
    fun scanResultQueueListenerContainerFactory(
        sqsAsyncClient: SqsAsyncClient,
        @Value("\${scan-result-queue.listener.auto-startup}") autoStartup: Boolean,
        @Value("\${scan-result-queue.listener.max-concurrent-messages}") maxConcurrentMessages: Int,
        @Value("\${scan-result-queue.listener.initial-visibility-timeout-seconds}") initialVisibilityTimeoutSeconds: Int,
        @Value("\${scan-result-queue.listener.visibility-timeout-multiplier}") visibilityTimeoutMultiplier: Double,
        @Value("\${scan-result-queue.listener.max-visibility-timeout-seconds}") maxVisibilityTimeoutSeconds: Int,
    ): SqsMessageListenerContainerFactory<Any> =
        buildQueueListenerContainerFactory(
            sqsAsyncClient,
            QueueListenerSettings(
                autoStartup = autoStartup,
                maxConcurrentMessages = maxConcurrentMessages,
                initialVisibilityTimeoutSeconds = initialVisibilityTimeoutSeconds,
                visibilityTimeoutMultiplier = visibilityTimeoutMultiplier,
                maxVisibilityTimeoutSeconds = maxVisibilityTimeoutSeconds,
            ),
            persistentFailureTypes =
                setOf(
                    InvalidScanResultMessageException::class,
                    PersistentScanResultProcessingException::class,
                    SerializationException::class,
                ),
        )

    companion object {
        const val SCAN_RESULT_QUEUE_LISTENER_PROFILES = "scan-result-worker | (local & !web-server-deactivated)"
        const val SCAN_RESULT_QUEUE_LISTENER_CONTAINER_FACTORY = "scanResultQueueListenerContainerFactory"
    }
}
