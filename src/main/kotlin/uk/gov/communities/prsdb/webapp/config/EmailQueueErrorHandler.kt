package uk.gov.communities.prsdb.webapp.config

import io.awspring.cloud.sqs.listener.errorhandler.AsyncErrorHandler
import io.awspring.cloud.sqs.listener.errorhandler.ErrorHandlerVisibilityHelper
import kotlinx.serialization.SerializationException
import org.springframework.messaging.Message
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import java.util.concurrent.CompletableFuture

// Persistent failures won't succeed on a later attempt, so they skip the exponential back-off and reach the dead letter queue quickly
class EmailQueueErrorHandler(
    private val persistentFailureVisibilityTimeoutSeconds: Int,
    private val transientFailureErrorHandler: AsyncErrorHandler<Any>,
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
        generateSequence(t) { it.cause }.any { it is PersistentEmailSendException || it is SerializationException }
}
