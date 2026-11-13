package uk.gov.communities.prsdb.webapp.config

import io.awspring.cloud.sqs.listener.SqsHeaders
import io.awspring.cloud.sqs.listener.Visibility
import io.awspring.cloud.sqs.listener.errorhandler.AsyncErrorHandler
import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class QueueListenerErrorHandlerTests {
    private val visibility: Visibility = mock()
    private val transientFailureErrorHandler: AsyncErrorHandler<Any> = mock()
    private val errorHandler =
        QueueListenerErrorHandler(
            PERSISTENT_FAILURE_VISIBILITY_TIMEOUT_SECONDS,
            transientFailureErrorHandler,
            setOf(PersistentEmailSendException::class, SerializationException::class),
        )
    private val message: Message<Any> =
        MessageBuilder
            .withPayload<Any>("payload")
            .setHeader(SqsHeaders.SQS_VISIBILITY_TIMEOUT_HEADER, visibility)
            .build()

    @ParameterizedTest
    @MethodSource("persistentFailures")
    fun `handle retries a persistent failure after a fixed visibility timeout`(persistentFailure: Throwable) {
        whenever(visibility.changeToAsync(any())).thenReturn(CompletableFuture.completedFuture(null))
        val listenerFailure = RuntimeException("Listener failed", persistentFailure)

        val result = errorHandler.handle(message, listenerFailure)

        verify(visibility).changeToAsync(PERSISTENT_FAILURE_VISIBILITY_TIMEOUT_SECONDS)
        verify(transientFailureErrorHandler, never()).handle(any<Message<Any>>(), any())
        val exception = assertThrows<ExecutionException> { result.get() }
        assertEquals(listenerFailure, exception.cause)
    }

    @Test
    fun `handle passes other failures to the transient failure error handler`() {
        val listenerFailure = RuntimeException("Listener failed", TransientEmailSentException("rate limited"))
        val transientFailureResult = CompletableFuture.failedFuture<Void>(listenerFailure)
        whenever(transientFailureErrorHandler.handle(message, listenerFailure)).thenReturn(transientFailureResult)

        val result = errorHandler.handle(message, listenerFailure)

        assertSame(transientFailureResult, result)
        verify(visibility, never()).changeToAsync(any())
    }

    @Test
    fun `handle passes failures of types not configured as persistent to the transient failure error handler`() {
        val handlerForOtherTypes =
            QueueListenerErrorHandler(
                PERSISTENT_FAILURE_VISIBILITY_TIMEOUT_SECONDS,
                transientFailureErrorHandler,
                setOf(IllegalStateException::class),
            )
        val listenerFailure = RuntimeException("Listener failed", PersistentEmailSendException("bad request"))
        val transientFailureResult = CompletableFuture.failedFuture<Void>(listenerFailure)
        whenever(transientFailureErrorHandler.handle(message, listenerFailure)).thenReturn(transientFailureResult)

        val result = handlerForOtherTypes.handle(message, listenerFailure)

        assertSame(transientFailureResult, result)
        verify(visibility, never()).changeToAsync(any())
    }

    companion object {
        private const val PERSISTENT_FAILURE_VISIBILITY_TIMEOUT_SECONDS = 30

        @JvmStatic
        fun persistentFailures() =
            listOf(
                PersistentEmailSendException("bad request"),
                SerializationException("undecodable"),
            )
    }
}
