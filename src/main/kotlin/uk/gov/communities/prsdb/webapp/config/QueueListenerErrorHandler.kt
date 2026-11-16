package uk.gov.communities.prsdb.webapp.config

import io.awspring.cloud.sqs.listener.errorhandler.AsyncErrorHandler
import io.awspring.cloud.sqs.listener.errorhandler.ErrorHandlerVisibilityHelper
import org.springframework.messaging.Message
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

// Persistent failures won't succeed on a later attempt, so they skip the exponential back-off and reach the dead letter queue quickly
class QueueListenerErrorHandler(
    private val persistentFailureVisibilityTimeoutSeconds: Int,
    private val transientFailureErrorHandler: AsyncErrorHandler<Any>,
    private val persistentFailureTypes: Set<KClass<out Throwable>>,
) : AsyncErrorHandler<Any> {
    override fun handle(
        message: Message<Any>,
        t: Throwable,
    ): CompletableFuture<Void> {
        if (!isPersistentFailure(t)) {
            return transientFailureErrorHandler.handle(message, t)
        }

        return ErrorHandlerVisibilityHelper
            .getVisibility(message)
            .changeToAsync(persistentFailureVisibilityTimeoutSeconds)
            .thenCompose { CompletableFuture.failedFuture(t) }
    }

    private fun isPersistentFailure(t: Throwable): Boolean =
        generateSequence(t) { it.cause }.any { cause -> persistentFailureTypes.any { it.isInstance(cause) } }
}
