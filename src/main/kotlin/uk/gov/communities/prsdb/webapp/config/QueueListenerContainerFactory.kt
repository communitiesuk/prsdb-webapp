package uk.gov.communities.prsdb.webapp.config

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory
import io.awspring.cloud.sqs.listener.QueueNotFoundStrategy
import io.awspring.cloud.sqs.listener.errorhandler.ExponentialBackoffErrorHandler
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import kotlin.reflect.KClass

data class QueueListenerSettings(
    val autoStartup: Boolean,
    val maxConcurrentMessages: Int,
    val initialVisibilityTimeoutSeconds: Int,
    val visibilityTimeoutMultiplier: Double,
    val maxVisibilityTimeoutSeconds: Int,
)

fun buildQueueListenerContainerFactory(
    sqsAsyncClient: SqsAsyncClient,
    settings: QueueListenerSettings,
    persistentFailureTypes: Set<KClass<out Throwable>>,
): SqsMessageListenerContainerFactory<Any> =
    SqsMessageListenerContainerFactory
        .builder<Any>()
        .sqsAsyncClient(sqsAsyncClient)
        .configure { options ->
            options
                .autoStartup(settings.autoStartup)
                .maxConcurrentMessages(settings.maxConcurrentMessages)
                .maxMessagesPerPoll(settings.maxConcurrentMessages)
                .queueNotFoundStrategy(QueueNotFoundStrategy.FAIL)
        }.errorHandler(
            QueueListenerErrorHandler(
                persistentFailureVisibilityTimeoutSeconds = settings.initialVisibilityTimeoutSeconds,
                transientFailureErrorHandler =
                    ExponentialBackoffErrorHandler
                        .builder<Any>()
                        .initialVisibilityTimeoutSeconds(settings.initialVisibilityTimeoutSeconds)
                        .multiplier(settings.visibilityTimeoutMultiplier)
                        .maxVisibilityTimeoutSeconds(settings.maxVisibilityTimeoutSeconds)
                        .build(),
                persistentFailureTypes = persistentFailureTypes,
            ),
        ).build()
